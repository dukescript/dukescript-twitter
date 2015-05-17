
package com.dukescript.twitterdemo.transfer;

import org.netbeans.html.context.spi.Contexts;
import org.netbeans.html.json.spi.Transfer;
import org.openide.util.lookup.ServiceProvider;

/**
 * @author antonepple
 */
@ServiceProvider(service = Contexts.Provider.class)
public class RegisterTransfer  implements Contexts.Provider {
    /** Registers a {@link Transfer} that works with a Twitter OAuth Bearer Token.
     *  It's registered at position 10.
     * 
     * @param context context to register the Transfer into
     * @param requestor ignored
     */
    @Override
    public void fillContext(Contexts.Builder context, Class<?> requestor) {
        context.register(Transfer.class, new AuthorizedTransfer(), 10);
    }
}
