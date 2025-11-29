package cn.encmys.ykdz.forest.hyphascript.node;

import cn.encmys.ykdz.forest.hyphascript.context.Context;
import cn.encmys.ykdz.forest.hyphascript.exception.EvaluateException;
import cn.encmys.ykdz.forest.hyphascript.oop.ScriptObject;
import cn.encmys.ykdz.forest.hyphascript.oop.internal.InternalObjectManager;
import cn.encmys.ykdz.forest.hyphascript.lexer.token.Token;
import cn.encmys.ykdz.forest.hyphascript.value.Reference;
import cn.encmys.ykdz.forest.hyphascript.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class ClassDeclaration extends ASTNode {
    private final @NotNull String name;
    private final @Nullable ASTNode parent;
    private final @NotNull Map<String, ASTNode> members;
    private final @NotNull Map<String, ASTNode> staticMembers;

    public ClassDeclaration(@NotNull String name, @Nullable ASTNode parent, @NotNull Map<String, ASTNode> members, @NotNull Map<String, ASTNode> staticMembers, @NotNull Token startToken, @NotNull Token endToken) {
        super(startToken, endToken);
        this.name = name;
        this.parent = parent;
        this.members = members;
        this.staticMembers = staticMembers;
    }

    @Override
    public @NotNull Reference evaluate(@NotNull Context ctx) {
        ScriptObject parentClass = null;
        ScriptObject parentPrototype = InternalObjectManager.OBJECT_PROTOTYPE;
        if (parent != null) {
            try {
                parentClass = parent.evaluate(ctx).getReferredValue().getAsScriptObject();
                parentPrototype = parentClass.findMember("prototype").getReferredValue().getAsScriptObject();
            } catch (Exception e) {
                throw new EvaluateException(this, e.getMessage(), e);
            }
        }

        // Class.prototype == Object.create(Parent.prototype)
        ScriptObject classPrototype = new ScriptObject(parentPrototype);
        // Class.prototype.getName = () => this.name
        members.forEach((key, value) -> {
            Reference ref = value.evaluate(ctx);
            // 维护用于查找父类（构造函数）的 __home_object__ 属性
            // Class.prototype.getName.__home_object__ == Class.prototype
            try {
                ref.getReferredValue().getAsScriptObject().declareMember("__home_object__", new Reference(new Value(classPrototype)));
            } catch (Exception ignored) {
            }
            classPrototype.declareMember(key, ref);
        });

        // Class == Class.prototype.constructor
        ScriptObject classObj = classPrototype.findMember("constructor").getReferredValue().getAsScriptObject();
        classObj.declareMember("prototype", new Reference(new Value(classPrototype)));

        // Class.staticMethod = () => console.log("static");
        staticMembers.forEach((key, value) -> {
            Reference ref = value.evaluate(ctx);
            // 维护用于查找父类（构造函数）的 __home_object__ 属性
            // Class.staticMethod.__home_object__ == Class
            try {
                ref.getReferredValue().getAsScriptObject().declareMember("__home_object__", new Reference(new Value(classObj)));
            } catch (Exception ignored) {
            }
            classObj.declareMember(key, ref);
        });

        ctx.declareMember(name, new Reference(new Value(classObj)));
        // Class.__proto__ == Parent
        if (parent != null) {
            classObj.setProto(parentClass);
        } else {
            classObj.setProto(InternalObjectManager.FUNCTION_PROTOTYPE);
        }

        return new Reference();
    }
}
