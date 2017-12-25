package pl.margoj.editor2.editor.operation

import pl.margoj.utils.javafx.operation.Operation
import pl.margoj.utils.javafx.operation.OperationCallback

abstract class SimpleOperation<T : SimpleOperation<T>> : Operation<T>
{
    @Suppress("UNCHECKED_CAST")
    override final fun start(operationCallback: OperationCallback<T>)
    {
        operationCallback.operationStarted(this as T)

        try
        {
            this.start0(operationCallback)
            operationCallback.operationFinished(this)
        }
        catch (e: Exception)
        {
            operationCallback.operationError(this, e)
        }
    }

    protected abstract fun start0(operationCallback: OperationCallback<T>)
}