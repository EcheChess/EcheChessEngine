package ca.watier.echechess.engine.interfaces;

public interface GamePropertiesHandler {
    boolean isAllowOtherToJoin();

    void setAllowOtherToJoin(boolean allowOtherToJoin);

    boolean isAllowObservers();

    void setAllowObservers(boolean allowObservers);
}
