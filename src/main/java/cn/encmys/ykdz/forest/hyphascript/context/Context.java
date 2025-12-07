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
    private @Nullable List<@NotNull String> importedJavaClasses;
    private @NotNull Config config = new Config(RoundingMode.HALF_UP, RoundingMode.HALF_UP, false, false);
    private @Nullable String scriptSource;

    /**
     * Construct a new context with GLOBAL_OBJECT as parent.
     *
     * @see InternalObjectManager#GLOBAL_OBJECT
     */
    public Context() {
        this(InternalObjectManager.GLOBAL_OBJECT);
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

    public @Nullable String getScriptSource() {
        return scriptSource;
    }

    public void setScriptSource(@Nullable String scriptSource) {
        this.scriptSource = scriptSource;
    }

    public @NotNull List<String> getImportedJavaClasses() {
        return importedJavaClasses == null ? Collections.emptyList()
                : Collections.unmodifiableList(importedJavaClasses);
    }

    public void addImportedJavaClasses(@NotNull String list) {
        if (importedJavaClasses == null) {
            importedJavaClasses = new ArrayList<>();
        }
        importedJavaClasses.add(list);
    }

    @Override
    public @NotNull Context clone() {
        Context cloned = new Context();
        cloned.members.putAll(this.members);
        cloned.__proto__ = this.__proto__;
        if (this.importedJavaClasses != null) {
            cloned.importedJavaClasses = new ArrayList<>(this.importedJavaClasses);
        }
        cloned.config = new Config(this.config.divRoundingMode, this.config.equalRoundingMode,
                this.config.runtimeTypeCheck, this.config.componentDecorationOverflow);
        return cloned;
    }

    public enum Type {
        NORMAL,
        FUNCTION,
        LOOP
    }

    public record Config(@NotNull RoundingMode divRoundingMode,
            @NotNull RoundingMode equalRoundingMode,
            boolean runtimeTypeCheck,
            boolean componentDecorationOverflow) {
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
         * @see InternalObjectManager#GLOBAL_OBJECT
         */
        @Contract(" -> new")
        public static @NotNull Builder create() {
            return new Builder();
        }

        /**
         * Create a context builder. The parent of this context will be provided parent
         * context
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