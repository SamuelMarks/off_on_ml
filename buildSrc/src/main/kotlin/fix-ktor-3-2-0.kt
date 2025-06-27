/*import org.objectweb.asm.ClassVisitor

class FieldSkippingClassVisitor(
    apiVersion: Int,
    nextClassVisitor: ClassVisitor,
) : ClassVisitor(apiVersion, nextClassVisitor) {

    // Returning null from this method will cause the ClassVisitor to strip all fields from the class.
    override fun visitField(
        access: Int,
        name: String?,
        descriptor: String?,
        signature: String?,
        value: Any?
    ): FieldVisitor? = null

    abstract class Factory : AsmClassVisitorFactory<Parameters> {

        private val excludedClasses
            get() = parameters.get().classes.get()

        override fun isInstrumentable(classData: ClassData): Boolean =
            classData.className in excludedClasses

        override fun createClassVisitor(classContext: ClassContext, nextClassVisitor: ClassVisitor): ClassVisitor {
            return FieldSkippingClassVisitor(
                apiVersion = instrumentationContext.apiVersion.get(),
                nextClassVisitor = nextClassVisitor,
            )
        }
    }

    abstract class Parameters : InstrumentationParameters {
        @get:Input
        abstract val classes: SetProperty<String>
    }
}*/
