package cn.encmys.ykdz.forest.hyphascript.context;

import cn.encmys.ykdz.forest.hyphascript.exception.ContextException;
import cn.encmys.ykdz.forest.hyphascript.value.Reference;
import cn.encmys.ykdz.forest.hyphascript.value.Value;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

public class Context implements Cloneable {
    public enum Type {
        NORMAL,
        FUNCTION,
        LOOP
    }

    /**
     * Global context used to register some common utils function
     * or variable to the script.
     */
    @NotNull
    public final static Context GLOBAL_CONTEXT = new Context();

    @NotNull
    private final Map<String, Reference> members = new HashMap<>();
    @NotNull
    private final Map<Integer, String> importedMembers = new HashMap<>();
    @NotNull
    private final List<String> importedJavaClasses = new ArrayList<>();
    @Nullable
    private Context parent;
    @NotNull
    private final Type type;
    @NotNull
    private Config config = new Config(RoundingMode.HALF_UP, RoundingMode.HALF_UP);

    /**
     * Construct a new context with GLOBAL_CONTEXT as parent.
     * @see #GLOBAL_CONTEXT
     */
    public Context() {
        this.parent = GLOBAL_CONTEXT;
        this.type = Type.NORMAL;
    }

    /**
     * Construct a new context with given type and parent as parent.
     * @param type Type of this context
     * @param parent Parent of this context
     */
    public Context(@NotNull Type type, @Nullable Context parent) {
        this.type = type;
        this.parent = parent;
    }

    public void setParent(@Nullable Context parent) {
        this.parent = parent;
    }

    public @Nullable Context getParent() {
        return parent;
    }

    /**
     * Declare a reference with null as init value
     * @param name Name of this variable
     * @param isConst Whether this variable is const
     * @param isExported Whether this variable is exported
     * @throws ContextException Throw when variable with given name are already exists in this context
     */
    public void declareReference(@NotNull String name, boolean isConst, boolean isExported) {
        if (members.containsKey(name)) {
            throw new ContextException(this, "Variable already declared in this context: " + name);
        }
        declareReference(name, new Reference(name, new Value(null), isConst, isExported));
    }

    /**
     * Declare a reference with given init value
     * @param name Name of this variable
     * @param initValue Init value of this variable which will be wrapper inside a Value object automatically
     * @param isConst Whether this variable is const
     * @throws ContextException Throw when variable with given name are already exists in this context
     */
    public void declareReference(@NotNull String name, @NotNull Value initValue, boolean isConst, boolean isExported) {
        if (members.containsKey(name)) {
            throw new ContextException(this, "Variable already declared in this context: " + name);
        }
        declareReference(name, new Reference(name, initValue, isConst, isExported));
    }

    /**
     * Declare a variable with given init value
     * @param name Name of this variable
     * @param initValue Init value of this variable
     * @throws ContextException Throw when variable with given name are already exists in this context
     */
    public void declareReference(@NotNull String name, @NotNull Value initValue) {
        if (members.containsKey(name)) {
            throw new ContextException(this, "Variable already declared in this context: " + name);
        }
        declareReference(name, new Reference(name, initValue));
    }

    public void declareReference(@NotNull String name, @NotNull Reference reference) {
        if (members.containsKey(name)) {
            throw new ContextException(this, "Variable already declared in this context: " + name);
        }
        members.put(name, reference);
    }

    /**
     * Change the value of a variable that is already exists in current or parent context
     * @param name Name of target variable
     * @param value New value of target variable
     * @throws ContextException Throw when variable with given name has not declared yet in this and parent context
     */
    public void putMember(@NotNull String name, @NotNull Value value) {
        if (hasLocalMember(name)) {
            members.get(name).setReferredValue(value);
        } else if (parent != null && parent.hasMember(name)) {
            parent.putMember(name, value);
        } else {
            throw new ContextException(this, "Variable not declared: " + name);
        }
    }

    @NotNull
    public Reference findMember(@NotNull String name) {
        if (hasLocalMember(name)) {
            return members.get(name);
        } else if (parent != null) {
            return parent.findMember(name);
        }
        return new Reference(name, new Value(null));
//        throw new ContextException(this, "Variable not found: " + name);
    }

    public boolean hasMember(@NotNull String name) {
        return hasLocalMember(name) || (parent != null && parent.hasMember(name));
    }

    public boolean hasLocalMember(@NotNull String name) {
        return members.containsKey(name);
    }

    @NotNull
    public Type getType() {
        return type;
    }

    public @NotNull Config getConfig() {
        return config;
    }

    public void setConfig(@NotNull Config config) {
        this.config = config;
    }

    @NotNull
    public Map<String, Reference> getMembers() {
        return members;
    }

    @NotNull
    public Map<String, Reference> getExportedMembers() {
        return members.entrySet().stream()
                .filter(entry -> entry.getValue().isExported())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @NotNull
    public Reference getNestedMember(@NotNull String path) {
        String[] nodes = path.split("\\.");
        if (nodes.length == 0) return new Reference();
        try {
            if (nodes.length == 1) return findMember(nodes[0]);
            Reference currentMember = findMember(nodes[0]);
            for (String node : Arrays.copyOfRange(nodes, 1, nodes.length)) {
                if (!currentMember.getReferedValue().isType(Value.Type.NESTED_OBJECT))
                    return new Reference();
                Map<String, Reference> members = currentMember.getReferedValue().getAsNestedObject();
                if (!members.containsKey(node)) return new Reference();
                currentMember = members.get(node);
            }
            return currentMember;
        } catch (ContextException ignored) {
            return new Reference();
        }
    }

    /**
     * Get the origin of an imported member by its hash.
     * @param hash The hash of the imported member
     * @return The origin of the imported member, or null if not found
     */
    @NotNull
    public String getImportMemberOrigin(int hash) {
        if (importedMembers.containsKey(hash)) {
            return importedMembers.get(hash);
        } else if (parent != null) {
            return parent.getImportMemberOrigin(hash);
        }
        throw new ContextException(this, "Import not found: " + hash);
    }

    /**
     * Register the origin of an imported member by its hash.
     * @param hash The hash of the imported member
     * @param from The origin of the imported member
     */
    public void putImportedMemberOrigin(int hash, @NotNull String from) {
        importedMembers.put(hash, from);
    }

    /**
     * Check if a member is imported by its hash.
     * @param hash The hash of the member
     * @return True if the member is imported, false otherwise
     */
    public boolean isImportedMember(int hash) {
        return importedMembers.containsKey(hash) || (parent != null && parent.isImportedMember(hash));
    }

    public @NotNull List<String> getImportedJavaClasses() {
        return Collections.unmodifiableList(importedJavaClasses);
    }

    public void addImportedJavaClasses(@NotNull String list) {
        importedJavaClasses.add(list);
    }

    @Override
    public Context clone() {
        try {
            Context cloned = (Context) super.clone();
            cloned.members.putAll(this.members);
            cloned.importedMembers.putAll(this.importedMembers);
            cloned.importedJavaClasses.addAll(this.importedJavaClasses);
            cloned.config = new Config(this.config.divRoundingMode, this.config.equalRoundingMode);
            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    @Override
    public String toString() {
        return "Context{" +
                "members=" + members +
                ", parent=" + parent +
                ", type=" + type +
                ", config=" + config +
                '}';
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
            this.context = new Context(Type.NORMAL, context);
        }

        /**
         * Create a context builder. The parent of this context will be GLOBAL_CONTEXT
         * @return Builder
         * @see #GLOBAL_CONTEXT
         */
        @Contract(" -> new")
        public static @NotNull Builder create() {
            return new Builder();
        }

        /**
         * Create a context builder. The parent of this context will be provided parent context
         * @return Builder
         */
        @Contract("_ -> new")
        public static @NotNull Builder create(@NotNull Context parent) {
            return new Builder(parent);
        }

        public Builder with(@NotNull String name, @NotNull Value value) {
            context.declareReference(name, value);
            return this;
        }

        public Builder with(@NotNull Config config) {
            context.setConfig(config);
            return this;
        }

        public Context build() {
            return context;
        }
    }
}