package cn.encmys.ykdz.forest.hyphascript.parser;

import cn.encmys.ykdz.forest.hyphascript.token.Token;
import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;
import java.util.Map;

public class PrecedenceTable {
    private final @NotNull Map<Token.Type, Precedence> precedences = new EnumMap<>(Token.Type.class);

    void put(@NotNull Token.Type type, @NotNull Precedence precedence) {
        precedences.put(type, precedence);
    }

    @NotNull Precedence get(@NotNull Token.Type type) {
        return precedences.getOrDefault(type, Precedence.LOWEST);
    }

    public enum Precedence {
        LOWEST(0),
        ASSIGNMENT(1),      // = := += -= *= /= %= ^=
        CONDITIONAL(2),     // ? :
        LOGIC_OR(3),        // ||
        LOGIC_AND(4),       // &&
        BIT_OR(5),        // |
        BIT_AND(6),       // &
        EQUALITY(7),        // == !=
        COMPARISON(8),      // < <= > >=
        PLUS_MINUS(9),            // + -
        MUL_DIV_MOD(10),          // * / %
        POWER(11),           // ^
        UNARY(12),          // typeof ! -
        CALL(13),           // ()
        MEMBER_ACCESS(14);         // . []

        final int level;

        Precedence(int level) {
            this.level = level;
        }

        boolean lessThan(@NotNull Precedence other) {
            return this.level < other.level;
        }
    }
}