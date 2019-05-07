package ca.watier.echechess.engine.handlers;

import ca.watier.echechess.engine.interfaces.GamePropertiesHandler;

public class GamePropertiesHandlerImpl implements GamePropertiesHandler {

    private boolean allowOtherToJoin = false;
    private boolean allowObservers = false;

    public GamePropertiesHandlerImpl() {
    }

    @Override
    public boolean isAllowOtherToJoin() {
        return allowOtherToJoin;
    }

    @Override
    public void setAllowOtherToJoin(boolean allowOtherToJoin) {
        this.allowOtherToJoin = allowOtherToJoin;
    }

    @Override
    public boolean isAllowObservers() {
        return allowObservers;
    }

    @Override
    public void setAllowObservers(boolean allowObservers) {
        this.allowObservers = allowObservers;
    }
}
