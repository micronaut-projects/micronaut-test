/*
 * Copyright 2017-2024 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.test.typepollution;

import net.bytebuddy.asm.AsmVisitorWrapper;
import net.bytebuddy.description.field.FieldDescription;
import net.bytebuddy.description.field.FieldList;
import net.bytebuddy.description.method.MethodList;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.jar.asm.ClassVisitor;
import net.bytebuddy.jar.asm.ClassWriter;
import net.bytebuddy.jar.asm.Handle;
import net.bytebuddy.jar.asm.Label;
import net.bytebuddy.jar.asm.MethodVisitor;
import net.bytebuddy.jar.asm.Opcodes;
import net.bytebuddy.jar.asm.Type;
import net.bytebuddy.pool.TypePool;

import java.lang.reflect.Method;

final class VisitorWrapperImpl extends AsmVisitorWrapper.AbstractBase {
    @Override
    public int mergeWriter(int flags) {
        return flags | ClassWriter.COMPUTE_FRAMES;
    }

    @Override
    public ClassVisitor wrap(TypeDescription instrumentedType, ClassVisitor classVisitor, Implementation.Context implementationContext, TypePool typePool, FieldList<FieldDescription.InDefinedShape> fields, MethodList<?> methods, int writerFlags, int readerFlags) {
        return new ClassVisitorImpl(Opcodes.ASM9, classVisitor);
    }

    private static final class ClassVisitorImpl extends ClassVisitor {
        ClassVisitorImpl(int api, ClassVisitor classVisitor) {
            super(api, classVisitor);
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            super.visit(Math.max(version, Opcodes.V1_8), access, name, signature, superName, interfaces);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
            return new MethodVisitorImpl(Opcodes.ASM9, super.visitMethod(access, name, descriptor, signature, exceptions));
        }
    }

    private static final class MethodVisitorImpl extends MethodVisitor {
        MethodVisitorImpl(int api, MethodVisitor methodVisitor) {
            super(api, methodVisitor);
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
            if (opcode == Opcodes.INVOKEVIRTUAL && owner.equals(Type.getInternalName(Class.class))) {
                if (name.equals("cast")) {
                    // itf ref
                    super.visitInsn(Opcodes.DUP2);
                    // itf ref itf ref
                    super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
                    Label skip = new Label();
                    Label end = new Label();
                    // itf ref ref
                    super.visitJumpInsn(Opcodes.IFNULL, skip);
                    // itf ref
                    indy(HookBootstrap.METHOD_DYNAMIC_TYPE_CHECK_CAST, Type.getMethodDescriptor(Type.getType(Object.class), Type.getType(Class.class), Type.getType(Object.class)));
                    // ref
                    super.visitJumpInsn(Opcodes.GOTO, end);

                    super.visitLabel(skip);
                    // itf ref
                    super.visitInsn(Opcodes.SWAP);
                    // ref itf
                    super.visitInsn(Opcodes.POP);
                    // ref
                    super.visitLabel(end);
                    //
                } else if (name.equals("isInstance")) {
                    // itf ref
                    super.visitInsn(Opcodes.DUP2);
                    // itf ref itf ref
                    super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
                    // itf ref boolean
                    Label skip = new Label();
                    Label end = new Label();
                    super.visitJumpInsn(Opcodes.IFEQ, skip);
                    // itf ref
                    invokeGetClass();
                    // itf concrete
                    dynamicTypeCheckSucceeded();
                    //
                    super.visitInsn(Opcodes.ICONST_1);
                    // boolean
                    super.visitJumpInsn(Opcodes.GOTO, end);

                    super.visitLabel(skip);
                    // itf ref
                    super.visitInsn(Opcodes.POP2);
                    //
                    super.visitInsn(Opcodes.ICONST_0);
                    // boolean
                    super.visitLabel(end);
                    // boolean
                } else if (name.equals("isAssignableFrom")) {
                    // itf concrete
                    super.visitInsn(Opcodes.DUP2);
                    // itf concrete itf concrete
                    super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
                    // itf concrete boolean
                    Label skip = new Label();
                    Label end = new Label();
                    super.visitJumpInsn(Opcodes.IFEQ, skip);
                    // itf concrete
                    dynamicTypeCheckSucceeded();
                    //
                    super.visitInsn(Opcodes.ICONST_1);
                    // boolean
                    super.visitJumpInsn(Opcodes.GOTO, end);

                    super.visitLabel(skip);
                    // itf concrete
                    super.visitInsn(Opcodes.POP2);
                    //
                    super.visitInsn(Opcodes.ICONST_0);
                    // boolean
                    super.visitLabel(end);
                    // boolean
                } else {
                    super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
                }
            } else {
                super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
            }
        }

        private void invokeGetClass() {
            super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Type.getInternalName(Object.class), "getClass", Type.getMethodDescriptor(Type.getType(Class.class)), false);
        }

        private void dynamicTypeCheckSucceeded() {
            indy(HookBootstrap.METHOD_DYNAMIC_TYPE_CHECK, Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(Class.class), Type.getType(Class.class)));
        }

        @Override
        public void visitTypeInsn(int opcode, String type) {
            // line comments show the current stack
            if (opcode == Opcodes.INSTANCEOF) {
                // ref
                super.visitInsn(Opcodes.DUP);
                // ref ref
                super.visitTypeInsn(opcode, type);
                // ref boolean
                Label skip = new Label();
                Label end = new Label();
                super.visitJumpInsn(Opcodes.IFEQ, skip);
                // ref
                staticTypeCheckSucceeded(type);
                //
                super.visitInsn(Opcodes.ICONST_1);
                // boolean
                super.visitJumpInsn(Opcodes.GOTO, end);

                super.visitLabel(skip);
                // ref
                super.visitInsn(Opcodes.POP);
                //
                super.visitInsn(Opcodes.ICONST_0);
                // boolean
                super.visitLabel(end);
            } else if (opcode == Opcodes.CHECKCAST) {
                // ref
                super.visitTypeInsn(opcode, type);
                // ref
                super.visitInsn(Opcodes.DUP);
                // ref ref
                Label skip = new Label();
                super.visitJumpInsn(Opcodes.IFNULL, skip);
                // ref
                super.visitInsn(Opcodes.DUP);
                // ref ref
                staticTypeCheckSucceeded(type);
                // ref
                super.visitLabel(skip);
                // ref
            } else {
                super.visitTypeInsn(opcode, type);
            }
        }

        private void staticTypeCheckSucceeded(String type) {
            indy(HookBootstrap.METHOD_STATIC_TYPE_CHECK, Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(Object.class)), Type.getObjectType(type));
        }

        private void indy(Method bootstrapMethod, String callSiteDescriptor, Object... args) {
            super.visitInvokeDynamicInsn(
                "foo",
                callSiteDescriptor,
                new Handle(
                    Opcodes.H_INVOKESTATIC,
                    Type.getInternalName(bootstrapMethod.getDeclaringClass()),
                    bootstrapMethod.getName(),
                    Type.getMethodDescriptor(bootstrapMethod),
                    false
                ),
                args
            );
        }
    }
}
