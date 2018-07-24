# ASM 字节码生成相关

[官方文档-FAQ -10](https://asm.ow2.io/faq.html#Q10)

# 1. 简介

# 1. Textifier

通过`Textifier` 获取字节码指令本身

	java -classpath "asm.jar;asm-util.jar;yourjar.jar" org.objectweb.asm.util.Textifier org.domain.package.YourClass


# 2. ASMifier

通过`ASMifier` 获取字节码的ASM表达形式

	java -classpath "asm.jar;asm-util.jar" org.objectweb.asm.util.ASMifier org/domain/package/YourClass.class

-  这里的`org/domain/package/YourClass.class`改成待转换的类即可


# 3. Bytecode Outline插件

该插件只适用于Eclipse

[marketplace-Bytecode Outline](https://marketplace.eclipse.org/content/bytecode-outline)