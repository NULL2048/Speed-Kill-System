package pers.cy.speedkillsystem.redis;

public class SpeedKillKey extends BasePrefix {
    private SpeedKillKey(int expireSeconds, String prefix) {
        super(expireSeconds, prefix);
    }

    public static SpeedKillKey isGoodsOver = new SpeedKillKey(0, "go");

    public static SpeedKillKey getSpeedKillPath = new SpeedKillKey(60, "skp");

    public static SpeedKillKey getSpeedKillVerifyCode = new SpeedKillKey(300, "vc");
}
