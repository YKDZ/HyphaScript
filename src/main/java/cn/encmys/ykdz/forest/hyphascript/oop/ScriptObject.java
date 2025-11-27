package cn.encmys.ykdz.forest.hyphascript.oop;

import cn.encmys.ykdz.forest.hyphascript.exception.ScriptObjectException;
import cn.encmys.ykdz.forest.hyphascript.oop.internal.InternalObjectManager;
import cn.encmys.ykdz.forest.hyphascript.value.Reference;
import cn.encmys.ykdz.forest.hyphascript.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ScriptObject implements Cloneable {
    protected final @NotNull Map<String, Reference> members = new ConcurrentHashMap<>();
    private final @NotNull Map<@NotNull String, @NotNull Reference> exportedMembers = new ConcurrentHashMap<>();
    protected @NotNull Value __proto__;

    public ScriptObject() {
        this(InternalObjectManager.OBJECT_PROTOTYPE);
    }

    public ScriptObject(@Nullable ScriptObject __proto__) {
        this.__proto__ = new Value(__proto__);
    }

    public void declareMember(@NotNull Map<String, Reference> members) {
        this.members.putAll(members);
    }

    public void declareMember(@NotNull String name, @NotNull Value initValue) {
        declareMember(name, new Reference(initValue));
    }

    public void declareMember(@NotNull String name, @NotNull Reference reference) {
        if (hasLocalMember(name)) {
            throw new ScriptObjectException(this, "Member \"" + name + "\" already exists");
        }
        members.put(name, reference);
    }

    public void putMember(@NotNull String name, @NotNull Reference reference) {
        if (hasLocalMember(name)) {
            members.put(name, reference);
        } else if (!hasLocalMember(name) && !getProto().isType(Value.Type.NULL)) {
            getProto().getAsScriptObject().putMember(name, reference);
        } else {
            throw new ScriptObjectException(this, "Member " + name + " does not exists");
        }
    }

    public void putExportedMember(@NotNull String name, @NotNull Reference reference) {
        if (exportedMembers.containsKey(name))
            throw new ScriptObjectException(this, "Exported member \"" + name + "\" already exists");
        exportedMembers.put(name, reference);
    }

    public void deleteMember(@NotNull String name) {
        members.remove(name);
    }

    public void forceSetLocalMember(@NotNull String name, @NotNull Reference reference) {
        members.put(name, reference);
    }

    public @NotNull Reference findMember(@NotNull String name) {
        if (hasLocalMember(name)) {
            return members.get(name);
        } else if (!getProto().isType(Value.Type.NULL)) {
            return getProto().getAsScriptObject().findMember(name);
        }
        return new Reference(new Value(null));
    }

    public @NotNull Reference findMemberOrThrow(@NotNull String name) {
        if (hasLocalMember(name)) {
            return members.get(name);
        } else if (!getProto().isType(Value.Type.NULL)) {
            return getProto().getAsScriptObject().findMemberOrThrow(name);
        }
        throw new ScriptObjectException(this, "Member " + name + " does not exists");
    }

    public @NotNull Reference findLocalMemberOrCreateOne(@NotNull String name) {
        if (hasLocalMember(name)) {
            return members.get(name);
        } else {
            Reference init = new Reference(new Value(null));
            members.put(name, init);
            return init;
        }
    }

    public <T> @NotNull Optional<T> findMember(@NotNull String name, @NotNull Class<T> type) {
        Value value = findMember(name).getReferredValue();
        Value.Type target = Value.Type.fromClass(type);

        try {
            @SuppressWarnings("unchecked")
            T casted = (T) value.getAs(target);
            return Optional.ofNullable(casted);
        } catch (ClassCastException e) {
            return Optional.empty();
        }
    }

    public @NotNull Value getProto() {
        return __proto__;
    }

    public void setProto(@Nullable ScriptObject __proto__) {
        this.__proto__ = new Value(__proto__);
    }

    public boolean hasLocalMember(@NotNull String name) {
        return members.containsKey(name);
    }

    public boolean hasMember(@NotNull String name) {
        return hasLocalMember(name) || (!getProto().isType(Value.Type.NULL) && getProto().getAsScriptObject().hasMember(name));
    }

    public void putAllExportedMembers(@NotNull Map<String, Reference> exportedMembers) {
        this.exportedMembers.putAll(exportedMembers);
    }

    public void setExported(@NotNull String name) {
        setExported(name, name);
    }

    public void setExported(@NotNull String name, @NotNull String as) {
        if (!this.hasMember(name))
            throw new ScriptObjectException(this, "Exported member \"" + name + "\" does not exist");
        if (this.exportedMembers.containsKey(as)) return;
        exportedMembers.put(as, findMember(name));
    }

    public void setUnExported(@NotNull String name) {
        if (!this.hasMember(name)) throw new ScriptObjectException(this, "Exported member does not exist");
        if (!this.exportedMembers.containsKey(name)) return;
        exportedMembers.remove(name);
    }

    public @NotNull @Unmodifiable Map<@NotNull String, @NotNull Reference> getExportedMembers() {
        return Collections.unmodifiableMap(exportedMembers);
    }

    public @NotNull @Unmodifiable Map<String, Reference> getLocalMembers() {
        return Collections.unmodifiableMap(members);
    }

    public @NotNull ScriptObject newInstance() {
        return new ScriptObject(findMember("prototype").getReferredValue().getAsScriptObject());
    }

    /**
     * 解析成员路径 (如 "a.b.c")，逐层查找嵌套的 ScriptObject 成员
     *
     * @param path 点号分隔的成员路径
     * @return 最终成员的引用
     * @throws ScriptObjectException 路径无效、成员不存在或中间成员非 ScriptObject 时抛出
     */
    public @NotNull Reference findMemberWithPath(@NotNull String path) {
        // 校验路径格式
        if (path.trim().isEmpty()) {
            throw new ScriptObjectException(this, "Path cannot be null or empty");
        }

        // 分割路径并检查有效性
        String[] segments = path.split("\\.");
        if (segments.length == 0) {
            throw new ScriptObjectException(this, "Invalid path format: " + path);
        }

        ScriptObject currentObj = this;
        for (int i = 0; i < segments.length; i++) {
            String segment = segments[i].trim();
            if (segment.isEmpty()) {
                throw new ScriptObjectException(this, "Path contains empty segment: " + path);
            }

            Reference memberRef;
            try {
                memberRef = currentObj.findMember(segment);
            } catch (Exception ex) {
                throw new ScriptObjectException(currentObj,
                        "Missing member '" + segment + "' in path: " + path, ex);
            }

            if (i == segments.length - 1) {
                return memberRef;
            }

            // 检查中间成员类型必须为 ScriptObject
            Value memberValue = memberRef.getReferredValue();
            if (!memberValue.isType(Value.Type.SCRIPT_OBJECT, Value.Type.FUNCTION)) {
                throw new ScriptObjectException(currentObj,
                        "Member '" + segment + "' is not a ScriptObject in path: " + path);
            }

            currentObj = memberValue.getAsScriptObject();
        }

        throw new ScriptObjectException(this, "Impossible error");
    }

    public <T> @NotNull Optional<T> findMemberWithPathSafely(@NotNull String path, @NotNull Class<T> type) {
        try {
            Reference ref = findMemberWithPath(path);
            Value value = ref.getReferredValue();
            Value.Type targetType = Value.Type.fromClass(type);

            if (value.getType() != targetType) {
                return Optional.empty();
            }

            try {
                @SuppressWarnings("unchecked")
                T casted = (T) value.getAs(targetType);
                return Optional.ofNullable(casted);
            } catch (ClassCastException e) {
                return Optional.empty();
            }
        } catch (ScriptObjectException | IllegalStateException e) {
            return Optional.empty();
        }
    }

    public @NotNull Optional<@NotNull Value> findMemberWithPathSafely(@NotNull String path) {
        try {
            Value value = findMemberWithPath(path).getReferredValue();
            try {
                return Optional.of(value);
            } catch (ClassCastException e) {
                return Optional.empty();
            }
        } catch (ScriptObjectException | IllegalStateException e) {
            return Optional.empty();
        }
    }

    @Override
    protected ScriptObject clone() throws CloneNotSupportedException {
        ScriptObject cloned = new ScriptObject(this.__proto__.getAsScriptObject());
        cloned.members.putAll(this.members);
        cloned.exportedMembers.putAll(this.exportedMembers);
        return cloned;
    }

    @Override
    public @NotNull String toString() {
        Map<String, Reference> temp = new HashMap<>(exportedMembers);
        temp.putAll(members);
        return temp.entrySet().stream()
                .map(e -> {
                    // 可能导致循环引用
                    if (e.getKey().startsWith("__")) return null;
                    try {
                        return (exportedMembers.containsKey(e.getKey()) ? "*" : "") + e.getKey() + ": " + e.getValue().getReferredValue().toReadableString();
                    } catch (Exception ex) {
                        return e.getKey() + ": [Error]";
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.joining(", ", "{", "}"));
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ScriptObject object = (ScriptObject) o;
        return Objects.equals(members, object.members) && Objects.equals(__proto__, object.__proto__);
    }

    @Override
    public int hashCode() {
        return Objects.hash(members, __proto__);
    }

    public static class Builder {
        private final ScriptObject object;

        public Builder() {
            object = new ScriptObject();
        }

        public Builder(@Nullable ScriptObject __proto__) {
            object = new ScriptObject(__proto__);
        }

        public Builder withMember(@NotNull String name, @NotNull Reference reference) {
            object.declareMember(name, reference);
            return this;
        }

        public @NotNull ScriptObject build() {
            return object;
        }
    }
}
