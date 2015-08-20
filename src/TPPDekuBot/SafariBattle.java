package TPPDekuBot;

import java.security.SecureRandom;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class SafariBattle {

    public Trainer user;
    private Pokemon wild;
    private int angry = 0, eat = 0;
    public LinkedBlockingQueue<String> msg = new LinkedBlockingQueue<>();
    private double hpMax = wild.getMaxHP();
    private double hpCurrent = wild.getStat(Stats.HP);
    private double rate = Pokemon.getCatchRate(wild.getName());
    private double catchChance = (((3 * hpMax - (2 * hpCurrent)) * rate * 1.5) / 3 * hpMax);
    private double shakeProbability = 1048560.0 / Math.sqrt(Math.sqrt(16711680 / catchChance));

    public SafariBattle(String user, Pokemon wild) {
        this.user = new Trainer(user);
        this.wild = wild;
    }

    public SafariBattle(Trainer user, Pokemon wild) {
        this.user = user;
        this.wild = wild;
    }

    private void recalcCatch() {
        hpMax = wild.getMaxHP();
        hpCurrent = wild.getStat(Stats.HP);
        rate = Pokemon.getCatchRate(wild.getName());
        catchChance = (((3 * hpMax - (2 * hpCurrent)) * rate * 1.5) / 3 * hpMax);
        shakeProbability = 1048560.0 / Math.sqrt(Math.sqrt(16711680 / catchChance));
    }

    public void doBattle(BattleBot b, String channel) {
        b.sendMessage(channel, "A Wild " + wild.getName() + " Appeared!");
        boolean caught = false;
        String lastTurn = "";
        while (true) {
            msg = new LinkedBlockingQueue<>();
            b.sendMessage(channel, "What will " + user + " do? (!bait) Throw Bait, (!rock) Throw Rock, (!ball) Throw Pokeball, (!run) Run");
            try {
                String move = msg.poll(60, TimeUnit.SECONDS);
                if (move == null) {
                    b.sendMessage(channel, user.getTrainerName() + " did not select an action in time, the Pokemon was stolen by Team Flare WutFace");
                    return;
                }
                if (move.startsWith("!bait")) {
                    lastTurn = "bait";
                    angry = 0;
                    eat += new SecureRandom().nextInt(5 - 1 + 1) + 1;

                } else if (move.startsWith("!rock")) {
                    lastTurn = "rock";
                    angry += new SecureRandom().nextInt(5 - 1 + 1) + 1;
                    eat = 0;
                } else if (move.startsWith("!ball")) {
                    lastTurn = "ball";
                    b.sendMessage(channel, user.getTrainerName() + " threw a Pokeball!");
                    recalcCatch();
                    int shake1 = new SecureRandom().nextInt(65536);
                    if (shake1 >= shakeProbability) {
                        b.sendMessage(channel, "Oh no! The Pokemon broke free! RuleFive");
                        continue;
                    }
                    b.sendMessage(channel, "Shake...");
                    int shake2 = new SecureRandom().nextInt(65536);
                    if (shake2 >= shakeProbability) {
                        b.sendMessage(channel, "Aww! It appeared to be caught! BibleThump");
                        continue;
                    }
                    b.sendMessage(channel, "Shake.... ThunBeast");
                    int shake3 = new SecureRandom().nextInt(65536);
                    if (shake3 >= shakeProbability) {
                        b.sendMessage(channel, "Aargh! Almost had it caught! DansGame");
                        continue;
                    }
                    b.sendMessage(channel, "Shake..... PogChamp");
                    int shake4 = new SecureRandom().nextInt(65536);
                    if (shake4 >= shakeProbability) {
                        b.sendMessage(channel, "Gah! It was so close, too! SwiftRage");
                        continue;
                    }
                    b.sendMessage(channel, "Awright! " + wild.getName() + " was caught! Kreygasm");
                    caught = true;
                    return;

                } else if (move.startsWith("!run")) {
                    b.sendMessage(channel, "You got away safely!");
                    lastTurn = "run";
                    return;
                }
            } catch (Exception ex) {
            } finally {
                if (caught || lastTurn.equalsIgnoreCase("run") || lastTurn.equalsIgnoreCase("ball")) {
                    return;
                }
                if (angry > 0) {
                    angry--;
                }
                if (eat > 0) {
                    eat--;
                }
                if (angry == 0) {
                    recalcCatch();
                }
                switch (lastTurn) {
                    case "bait":
                        b.sendMessage(channel, wild.getName() + " is eating!");
                        catchChance = catchChance / 2;
                        int random = new SecureRandom().nextInt(256);
                        if (random < (wild.getStat(Stats.SPEED) / 2)) {
                            b.sendMessage(channel, "The wild " + wild.getName() + " ran away!");
                            return;
                        }
                        continue;
                    case "rock":
                        b.sendMessage(channel, wild.getName() + " is pissed off!");
                        catchChance = catchChance * 2;
                        if (catchChance > 255) {
                            catchChance = 255;
                        }
                        random = new SecureRandom().nextInt(256);
                        if (random > (wild.getStat(Stats.SPEED) * 4)) {
                            b.sendMessage(channel, "The wild " + wild.getName() + " ran away!");
                            return;
                        }
                        continue;
                    default:
                        b.sendMessage(channel, wild.getName() + " is watching carefully...");
                        random = new SecureRandom().nextInt(256);
                        if (random > (wild.getStat(Stats.SPEED) * 2)) {
                            b.sendMessage(channel, "The wild " + wild.getName() + " ran away!");
                            return;
                        }
                        continue;
                }

            }
        }
    }
}