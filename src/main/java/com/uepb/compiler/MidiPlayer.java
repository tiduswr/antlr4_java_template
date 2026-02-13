package com.uepb.compiler;

import java.io.File;

import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Soundbank;
import javax.sound.midi.Synthesizer;

public class MidiPlayer {
    private Synthesizer synth;
    private MidiChannel[] channels;
    private Soundbank defaultSoundbank;
    private Soundbank customSoundbank;

    public MidiPlayer() {
        try {
            synth = MidiSystem.getSynthesizer();
            synth.open();

            Soundbank soundbank = synth.getDefaultSoundbank();
            defaultSoundbank = synth.getDefaultSoundbank();
            if (soundbank == null) {
            } else {
                synth.loadAllInstruments(soundbank);
            }

            channels = synth.getChannels();

            for (int i = 0; i < 16; i++) {
                if (channels[i] != null) {
                    channels[i].controlChange(91, 64);
                    channels[i].controlChange(7, 100);
                }
            }
        } catch (MidiUnavailableException e) {
            System.err.println("Erro: Dispositivo MIDI indisponível.");
        }
    }

    public void restoreDefaultSoundbank() {
        if (this.customSoundbank != null) {
            synth.unloadAllInstruments(this.customSoundbank);
            this.customSoundbank = null;
        }

        if (this.defaultSoundbank != null) {
            synth.loadAllInstruments(this.defaultSoundbank);
            System.out.println("Restaurado para o Soundbank padrão.");
        }
    }

    public void loadExternalSoundbank(String path) {
        try {
            File sf2File = new File(path);
            Soundbank sb = MidiSystem.getSoundbank(sf2File);

            if (this.customSoundbank != null) {
                synth.unloadAllInstruments(this.customSoundbank);
            }

            this.customSoundbank = sb;
            if (synth.loadAllInstruments(this.customSoundbank)) {
                System.out.println("Soundbank carregado: " + sb.getName());
            }
        } catch (Exception e) {
            System.err.println("Erro ao carregar Soundbank: " + e.getMessage());
        }
    }

    public void setVolume(int channelIndex, int vol) {
        if (channels != null && channelIndex < channels.length) {
            int midiVol = Math.max(0, Math.min(127, vol));
            channels[channelIndex].controlChange(7, midiVol);
        }
    }

    public void setInstrument(int channelIndex, int id) {
        if (channels != null && channelIndex < channels.length) {
            channels[channelIndex].programChange(id);
        }
    }

    public void play(int channelIndex, int note, int durationMs) {
        if (channels == null || channelIndex >= channels.length) return;
        MidiChannel chan = channels[channelIndex];
        chan.noteOn(note, 90);

        try {
            Thread.sleep(durationMs);
            chan.noteOff(note);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
}