package pl.margoj.editor2.editor.map.operation

import pl.margoj.editor2.operation.Operation
import pl.margoj.editor2.operation.OperationCallback

abstract class SimpleOperation<T: SimpleOperation<T>> : Operation<T>
{
    @Suppress("UNCHECKED_CAST")
    override final fun start(operationCallback: OperationCallback<T>)
    {
        operationCallback.operationStarted(this as T)

        try
        {
            this.start0(operationCallback)
            operationCallback.operationFinished(this as T)
        }
        catch (e: Exception)
        {
            operationCallback.operationError(this as T, e)
        }
    }

    protected abstract fun start0(operationCallback: OperationCallback<T>)
}