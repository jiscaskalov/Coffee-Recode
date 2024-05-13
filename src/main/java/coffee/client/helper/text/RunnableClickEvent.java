package coffee.client.helper.text;

/**
 * Allows arbitrary code execution in a click event
 * @see
 */
public class RunnableClickEvent extends CoffeeClickEvent {
    public final Runnable runnable;

    public RunnableClickEvent(Runnable runnable) {
        super(null, null); // Should ensure no vanilla code is triggered, and only we handle it
        this.runnable = runnable;
    }
}
