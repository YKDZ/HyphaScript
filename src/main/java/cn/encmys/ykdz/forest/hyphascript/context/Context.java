package cn.encmys.ykdz.forest.hyphascript.context;

import cn.encmys.ykdz.forest.hyphascript.oop.ScriptObject;
import cn.encmys.ykdz.forest.hyphascript.oop.internal.InternalObjectManager;
import cn.encmys.ykdz.forest.hyphascript.value.Value;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Context extends ScriptObject implements Cloneable {
    /**
     * Global object used to register some common utils function
     * or variable to the script.
     */
    @NotNull
    public final static Context GLOBAL_OBJECT = new Context(InternalObjectManager.OBJECT_PROTOTYPE);

    private @NotNull List<@NotNull String> importedJavaClasses = new ArrayList<>();
    private @NotNull Config config = new Config(RoundingMode.HALF_UP, RoundingMode.HALF_UP);

    /**
     * Construct a new context with GLOBAL_OBJECT as parent.
     *
     * @see #GLOBAL_OBJECT
     */
    public Context() {
        this(GLOBAL_OBJECT);
    }

    public Context(@Nullable ScriptObject parent) {
        super(parent);
    }

    public @Nullable Value getParent() {
        return getProto();
    }

    public void setParent(@Nullable Context parent) {
        setProto(parent);
    }

    public @NotNull Config getConfig() {
        return config;
    }

    public void setConfig(@NotNull Config config) {
        this.config = config;
    }

    public @NotNull List<String> getImportedJavaClasses() {
        return Collections.unmodifiableList(importedJavaClasses);
    }

    public void addImportedJavaClasses(@NotNull String list) {
        importedJavaClasses.add(list);
    }

    @Override
    public @NotNull Context clone() {
        Context cloned = new Context();
        cloned.members.putAll(this.members);
        cloned.__proto__ = this.__proto__;
        cloned.importedJavaClasses = this.importedJavaClasses;
        cloned.config = new Config(this.config.divRoundingMode, this.config.equalRoundingMode);
        return cloned;
    }

    public enum Type {
        NORMAL,
        FUNCTION,
        LOOP
    }

    public record Config(@NotNull RoundingMode divRoundingMode,
                         @NotNull RoundingMode equalRoundingMode) {
    }

    public static class Builder {
        private final Context context;

        private Builder() {
            this.context = new Context();
        }

        private Builder(@NotNull Context context) {
            this.context = new Context(context);
        }

        /**
         * Create a context builder. The parent of this context will be GLOBAL_OBJECT
         *
         * @return Builder
         * @see #GLOBAL_OBJECT
         */
        @Contract(" -> new")
        public static @NotNull Builder create() {
            return new Builder();
        }

        /**
         * Create a context builder. The parent of this context will be provided parent context
         *
         * @return Builder
         */
        @Contract("_ -> new")
        public static @NotNull Builder create(@NotNull Context parent) {
            return new Builder(parent);
        }

        public @NotNull Builder with(@NotNull String name, @NotNull Value value) {
            context.declareMember(name, value);
            return this;
        }

        public @NotNull Builder with(@NotNull Config config) {
            context.setConfig(config);
            return this;
        }

        public @NotNull Context build() {
            return context;
        }
    }
}