package pers.cy.speedkillsystem.redis;

public class SpeedKillKey extends BasePrefix {
    private SpeedKillKey(String prefix) {
        super(prefix);
    }

    public static SpeedKillKey isGoodsOver = new SpeedKillKey("go");
}
