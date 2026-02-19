package com.uepb.compiler;

import com.uepb.ExprBaseVisitor;
import com.uepb.ExprParser.ExponenciacaoContext;
import com.uepb.ExprParser.MulDivContext;
import com.uepb.ExprParser.NumeroContext;
import com.uepb.ExprParser.ParentesesContext;
import com.uepb.ExprParser.ProgContext;
import com.uepb.ExprParser.SomaSubContext;

public class Calculadora extends ExprBaseVisitor<Double>{
    
    @Override
    public Double visitProg(ProgContext ctx) {
        return visit(ctx.expr());
    }

    @Override
    public Double visitParenteses(ParentesesContext ctx) {
        return visit(ctx.NESTED_EXPR);
    }

    @Override
    public Double visitExponenciacao(ExponenciacaoContext ctx) {
        var base = visit(ctx.BASE);
        var expoente = visit(ctx.EXPOENTE);
        return Math.pow(base, expoente);
    }

    @Override
    public Double visitMulDiv(MulDivContext ctx) {
        var o1 = visit(ctx.O1);
        var o2 = visit(ctx.O2);
        var operador = ctx.OP.getText();

        if(operador.equals("/")){
            if(o2 == 0) throw new ArithmeticException("Divisão por zero!");

            return o1/o2;
        }else{
            return o1*o2;
        }
    }

    @Override
    public Double visitSomaSub(SomaSubContext ctx) {
        var o1 = visit(ctx.O1);
        var o2 = visit(ctx.O2);
        var operador = ctx.OP.getText();

        return operador.equals("+") ? o1+o2 : o1-o2;
    }

    @Override
    public Double visitNumero(NumeroContext ctx) {
        var numeroStr = ctx.NUMBER().getText();
        return Double.valueOf(numeroStr);
    }

}
