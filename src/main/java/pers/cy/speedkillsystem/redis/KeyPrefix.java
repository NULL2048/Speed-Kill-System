package pers.cy.speedkillsystem.redis;

public interface KeyPrefix {
    int expireSeconds();

    String getPrefix();
}
