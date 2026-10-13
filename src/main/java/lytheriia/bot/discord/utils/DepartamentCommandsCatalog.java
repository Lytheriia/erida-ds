package lytheriia.bot.discord.utils;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import lytheriia.bot.discord.interfaces.IBaseCommandCatalog;
import lytheriia.bot.discord.interfaces.IBaseDepartamentCommand;

@Component("departamentCommandsCatalog")
public class DepartamentCommandsCatalog extends IBaseCommandCatalog {

    public DepartamentCommandsCatalog(ObjectProvider<IBaseDepartamentCommand> commands) {
        super(commands);
    }
}