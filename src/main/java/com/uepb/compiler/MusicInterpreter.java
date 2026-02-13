package com.uepb.compiler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.uepb.MusicBaseVisitor;
import com.uepb.MusicParser;
import com.uepb.MusicParser.AssignContext;
import com.uepb.MusicParser.ExpressionContext;
import com.uepb.MusicParser.GroupContext;
import com.uepb.MusicParser.IdAtomContext;
import com.uepb.MusicParser.ImportSoundbankContext;
import com.uepb.MusicParser.NoteAtomContext;
import com.uepb.MusicParser.NoteContext;
import com.uepb.MusicParser.ParallelTrackContext;
import com.uepb.MusicParser.RepeatedExpContext;
import com.uepb.MusicParser.SetInstrumentContext;
import com.uepb.MusicParser.SetTempoContext;
import com.uepb.MusicParser.SetVolumeContext;

public class MusicInterpreter extends MusicBaseVisitor<Void> {
    private final Map<String, ExpressionContext> symbolTable = new HashMap<>();
    private final MidiPlayer player = new MidiPlayer();
    private final ThreadLocal<Integer> currentChannel = ThreadLocal.withInitial(() -> 0);
    private int bpm = 120;

    private static final Map<String, Integer> NOTES = Map.of(
        "C", 60, "D", 62, "E", 64, "F", 65, "G", 67, "A", 69, "B", 71
    );

    @Override
    public Void visitNote(NoteContext ctx) {
        long startTime = System.currentTimeMillis();

        int midiNote = NOTES.get(ctx.letter.getText().toUpperCase());
        if (ctx.accidental != null) 
            midiNote += (ctx.accidental.getType() == MusicParser.SHARP) ? 1 : -1;

        int octave = (ctx.octave != null) ? Integer.parseInt(ctx.octave.getText()) : 4;
        midiNote += (octave - 4) * 12;

        double beats = (ctx.duration() != null) ? Double.parseDouble(ctx.duration().getChild(1).getText()) : 1.0;
        int durationMs = (int) ((60000.0 / bpm) * beats);

        // Execução (90% do tempo é som ligado)
        int playTime = (int) (durationMs * 0.9);
        int restTime = durationMs - playTime;

        player.play(currentChannel.get(), midiNote, playTime);

        // Calcula quanto tempo o processamento (CPU/Midi) levou até aqui
        long elapsed = System.currentTimeMillis() - startTime;
        long actualWait = Math.max(0, restTime - (elapsed - playTime));

        sleep((int) actualWait);
        return null;
    }

    @Override
    public Void visitParallelTrack(ParallelTrackContext ctx) {
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < ctx.expression().size(); i++) {

            // Canal 9 é reservado pra bateria
            final int channel = (i >= 9) ? i + 1 : i;

            if (channel > 15){
                throw new RuntimeException("O MIDI só possuí 16 canais de áudio (o 9 é da bateria, mas não foi implementado)");
            };

            final var expr = ctx.expression(i);
            Thread t = new Thread(() -> {
                currentChannel.set(channel);
                visit(expr);
            });
            threads.add(t);
            t.start();
        }
        threads.forEach(t -> { try { t.join(); } catch (Exception ignored) {} });
        return null;
    }

    @Override
    public Void visitSetTempo(SetTempoContext ctx) {
        this.bpm = Integer.parseInt(ctx.INT().getText());
        return null;
    }

    @Override
    public Void visitSetInstrument(SetInstrumentContext ctx) {
        player.setInstrument(currentChannel.get(), Integer.parseInt(ctx.INT().getText()));
        return null;
    }

    @Override
    public Void visitSetVolume(SetVolumeContext ctx) {
        player.setVolume(currentChannel.get(), Integer.parseInt(ctx.INT().getText()));
        return null;
    }

    @Override
    public Void visitImportSoundbank(ImportSoundbankContext ctx) {
        if (ctx.STRING() != null) player.loadExternalSoundbank(ctx.STRING().getText().replace("\"", ""));
        else player.restoreDefaultSoundbank();
        return null;
    }

    @Override
    public Void visitAssign(AssignContext ctx) {
        symbolTable.put(ctx.assignment().ID().getText(), ctx.assignment().expression());
        return null;
    }

    @Override
    public Void visitIdAtom(IdAtomContext ctx) {
        ExpressionContext expr = symbolTable.get(ctx.ID().getText());
        if (expr != null) visit(expr);
        return null;
    }

    @Override
    public Void visitRepeatedExp(RepeatedExpContext ctx) {
        int times = Integer.parseInt(ctx.INT().getText());
        for (int i = 0; i < times; i++) visit(ctx.expression());
        return null;
    }

    @Override
    public Void visitGroup(GroupContext ctx) {
        ctx.statement().forEach(this::visit);
        return null;
    }

    @Override
    public Void visitNoteAtom(NoteAtomContext ctx) { return visit(ctx.note()); }

    private void sleep(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}