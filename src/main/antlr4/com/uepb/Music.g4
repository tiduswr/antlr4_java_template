grammar Music;

program: statement* EOF;

statement:  assignment                              # Assign
    |       expression                              # Exp
    |       expression '*' INT                      # RepeatedExp
    |       'tempo' INT                 # SetTempo
    |       'instr' instr=INT                       # SetInstrument
    |       'vol' INT                               # SetVolume
    |       'use' 'soundfont' (STRING | 'default')  # ImportSoundbank
    |       'parallel' '{' expression+ '}'          # ParallelTrack
;

assignment: ID '=' expression;

expression: note                                    # NoteAtom
    |       ID                                      # IdAtom
    |       '(' statement+ ')'                      # Group
;

note: letter=NOTE_LETTER accidental=(SHARP|FLAT)? octave=INT? duration?;
duration: ':' (INT | DECIMAL);

// --- REGRAS DE LEXER ---
NOTE_LETTER: [A-G];
SHARP: '#';
FLAT:  'b';
ID: '$' [a-zA-Z_][a-zA-Z0-9_]*;

DECIMAL: [0-9]+ '.' [0-9]+;
INT: [0-9]+;

STRING: '"' ~["]* '"';
WS: [ \n\r\t]+ -> skip;
COMMENT: '//' ~[\n\r]* -> skip;