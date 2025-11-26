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
        ASSIGNMENT(1),      // = := += -= *= /= %= **=
        CONDITIONAL(2),     // ? :
        LOGIC_OR(3),        // ||
        LOGIC_AND(4),       // &&
        BIT_OR(5),        // |
        XOR(6),           // ^
        BIT_AND(7),       // &
        EQUALITY(8),        // == !=
        COMPARISON(9),      // < <= > >=
        SHIFT_LEFT(10),     // <<
        SHIFT_RIGHT(10),    // >>
        PLUS_MINUS(11),            // + -
        MUL_DIV_MOD(12),          // * / %
        POWER(13),           // **
        UNARY(14),          // typeof ! - ~
        CALL(15),           // ()
        MEMBER_ACCESS(16),         // . []
        NOT(14);         // ~

        final int level;

        Precedence(int level) {
            this.level = level;
        }

        boolean lessThan(@NotNull Precedence other) {
            return this.level < other.level;
        }
    }
}