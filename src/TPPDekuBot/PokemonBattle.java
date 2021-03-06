package TPPDekuBot;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class PokemonBattle {

    private BattleBot b;
    private String channel;
    private String player;

    public PokemonBattle(BattleBot b, String channel, boolean bigBrother, boolean fromChef, String player, boolean test) {
        this.b = b;
        this.channel = channel;
        this.player = player;
        SecureRandom rand = new SecureRandom();
        int rand1 = rand.nextInt(718);
        int rand2 = rand1;
        while (rand2 == rand1) {
            rand2 = rand.nextInt(718);
        }
        if (Pokemon.isBannedPokemon(rand1) || Pokemon.isBannedPokemon(rand2)) {
            rand1 = rand.nextInt(718);
            rand2 = rand1;
            while (rand2 == rand1) {
                rand2 = rand.nextInt(718);
            }
        }
        int level1 = rand.nextInt(100 - 20 + 1) + 20;
        while (level1 < 20) {
            level1 = rand.nextInt(100 - 20 + 1) + 20;
        }
        int level1bufUpper = level1 + 7;
        int level1bufLower = level1 - 7;
        if (level1bufUpper > 100) {
            level1bufUpper = 100;
        }
        if (level1bufLower < 20) {
            level1bufLower = 20;
        }
        int level2 = rand.nextInt((level1bufUpper - level1bufLower) + 1) + level1bufLower;
        if (bigBrother) {
            if (fromChef) {
                level1 = 100;
                rand1 = 129;
            } else {
                level2 = 100;
                rand2 = 129;
            }
        }
        System.out.println("Generated userID " + rand1 + ", level " + level1 + " and computerID " + rand2 + ", level " + level2);
        Pokemon user = new Pokemon(rand1, level1);
        Pokemon computer = new Pokemon(rand2, level2);
        try {
            if (bigBrother) {
                if (fromChef) {
                    user.setMove1(Pokemon.getMove("Shadow-force"));
                    user.setMove2(Pokemon.getMove("Explosion"));
                    user.setMove3(Pokemon.getMove("Overheat"));
                    user.setMove4(Pokemon.getMove("Hydro-pump"));
                    user.setStat(Stats.HP, 1000);
                    user.setStat(Stats.ATTACK, 1000);
                    user.setStat(Stats.DEFENSE, 1000);
                    user.setStat(Stats.SPEED, 1000);
                    user.setStat(Stats.SP_ATTACK, 1000);
                    user.setStat(Stats.SP_DEFENSE, 1000);
                    computer.assignMoves();
                } else {
                    computer.setMove1(Pokemon.getMove("Shadow-force"));
                    computer.setMove2(Pokemon.getMove("Explosion"));
                    computer.setMove3(Pokemon.getMove("Overheat"));
                    computer.setMove4(Pokemon.getMove("Hydro-pump"));
                    computer.setStat(Stats.HP, 1000);
                    computer.setStat(Stats.ATTACK, 1000);
                    computer.setStat(Stats.DEFENSE, 1000);
                    computer.setStat(Stats.SPEED, 1000);
                    computer.setStat(Stats.SP_ATTACK, 1000);
                    computer.setStat(Stats.SP_DEFENSE, 1000);
                    user.assignMoves();
                }
            }
            if (user.getLevel() < 0 || computer.getLevel() < 0) {
                throw new Exception("Level is negative!!");
            }
            if (!bigBrother) {
                user.assignMoves();
                computer.assignMoves();
            }
        } catch (Exception ex) {
            System.err.println("[POKEMON] Failed to generate moves! UserID: " + rand1 + ", ComputerID: " + rand2 + "\n[POKEMON] " + ex);
            b.sendMessage(channel, "Something fucked up OneHand give it another try");
            b.inPokemonBattle = false;
            return;
        }
        if (test) {
            user = new Pokemon(63, 67);
            computer = new Pokemon(3, 73);
            HashMap<String, Move> moves = Pokemon.reloadMoves();
            user.setMove1(moves.get("Fake-out"));
            user.setMove2(moves.get("Mega-kick"));
            user.setMove3(moves.get("Zen-headbutt"));
            user.setMove4(moves.get("Body-slam"));
            computer.setMove1(moves.get("Hyper-beam"));
            computer.setMove2(moves.get("Take-down"));
            computer.setMove3(moves.get("Petal-dance"));
            computer.setMove4(moves.get("Echoed-voice"));
        }
        b.sendMessage(channel, "A wild " + computer.getName() + " (level " + level2 + ") appeared! Go " + user.getName() + "! (Level " + level1 + ")");
        System.err.println("User moves = " + user.getMove1().getName() + ", " + user.getMove2().getName() + ", " + user.getMove3().getName() + ", " + user.getMove4().getName() + ", ");
        System.err.println("Computer moves = " + computer.getMove1().getName() + ", " + computer.getMove2().getName() + ", " + computer.getMove3().getName() + ", " + computer.getMove4().getName() + ", ");
        try {
            while (!user.isFainted() && !computer.isFainted()) {
                BattleBot.pokemonMessages = new LinkedBlockingQueue<>();
                b.sendMessage(channel, "What will " + user.getName() + " do? (!move1)" + user.getMove1().getName() + ", (!move2)" + user.getMove2().getName() + ", (!move3)" + user.getMove3().getName() + ", (!move4)" + user.getMove4().getName());
                if (user.getStat(Stats.SPEED) > computer.getStat(Stats.SPEED)) {
                    String move = BattleBot.pokemonMessages.poll(60, TimeUnit.SECONDS);
                    if (move == null) {
                        b.sendMessage(channel, player + " did not select a move in time and got their Pokemon stolen by Team Rocket! RuleFive");
                        return;
                    }
                    if (move.equalsIgnoreCase("run")) {
                        b.sendMessage(channel, "You got away safely!");
                        return;
                    }
                    doUsersMove(user, computer, move);
                    if (computer.isFlinched()) {
                        b.sendMessage(channel, computer.getName() + " flinched!");
                        computer.setFlinch(false);
                    } else if (!computer.isFainted() && !user.isFainted()) {
                        doComputerMove(user, computer);
                    }
                } else if (user.getStat(Stats.SPEED) < computer.getStat(Stats.SPEED)) {
                    String move = BattleBot.pokemonMessages.poll(60, TimeUnit.SECONDS);
                    if (move == null) {
                        b.sendMessage(channel, player + " did not select a move in time and got their Pokemon stolen by Team Rocket! RuleFive");
                        return;
                    }
                    doComputerMove(user, computer);
                    if (user.isFlinched()) {
                        b.sendMessage(channel, user.getName() + " flinched!");
                        user.setFlinch(false);
                    } else if (!user.isFainted()) {
                        if (move.equalsIgnoreCase("run")) {
                            b.sendMessage(channel, "You got away safely!");
                            return;
                        }
                        doUsersMove(user, computer, move);
                    }
                } else {
                    rand = new SecureRandom();
                    int chance = rand.nextInt(2);
                    if (chance == 1) {
                        String move = BattleBot.pokemonMessages.poll(60, TimeUnit.SECONDS);
                        if (move == null) {
                            b.sendMessage(channel, player + " did not select a move in time and got their Pokemon stolen by Team Rocket! RuleFive");
                            return;
                        }
                        if (move.equalsIgnoreCase("run")) {
                            b.sendMessage(channel, "You got away safely!");
                            return;
                        }
                        doUsersMove(user, computer, move);
                        if (computer.isFlinched()) {
                            b.sendMessage(channel, computer.getName() + " flinched!");
                            computer.setFlinch(false);
                        } else if (!computer.isFainted()) {
                            doComputerMove(user, computer);
                        }
                    } else {
                        String move = BattleBot.pokemonMessages.poll(60, TimeUnit.SECONDS);
                        if (move == null) {
                            b.sendMessage(channel, player + " did not select a move in time and got their Pokemon stolen by Team Rocket! RuleFive");
                            return;
                        }
                        doComputerMove(user, computer);
                        if (user.isFlinched()) {
                            b.sendMessage(channel, user.getName() + " flinched!");
                            user.setFlinch(false);
                        } else if (!user.isFainted()) {
                            if (move.equalsIgnoreCase("run")) {
                                b.sendMessage(channel, "You got away safely!");
                                return;
                            }
                            doUsersMove(user, computer, move);
                        }
                    }
                }
            }
            if (user.isFainted()) {
                b.sendMessage(channel, user.getName() + " fainted! You lose! BibleThump");
            } else if (computer.isFainted()) {
                b.sendMessage(channel, computer.getName() + " fainted! You Win! PogChamp");
                if (user.getLevel() != 100) {
                    int levelBefore = user.getLevel();
                    int exp = Pokemon.calculateExperience(false, user, computer);
                    user.addExperience(exp);
                    int levelAfter = user.getLevel();
                    b.sendMessage(channel, user.getName() + " gained " + exp + " Exp. Points!");
                    if (levelBefore < levelAfter) {
                        b.sendMessage(channel, user.getName() + " grew to Level " + levelAfter + "! PogChamp");
                    }
                }
            }
        } catch (Exception ex) {

        }
        b.inPokemonBattle = false;
    }

    private void doComputerMove(Pokemon user, Pokemon computer) {
        b.sendMessage(channel, computer.attack(user, Move.selectBestMove(computer, user)).replace("\n", " "));
    }

    private void doUsersMove(Pokemon user, Pokemon computer, String move) {
        switch (move) {
            case "1":
                b.sendMessage(channel, user.attack(computer, user.getMove1()).replace("\n", " "));
                break;
            case "2":
                b.sendMessage(channel, user.attack(computer, user.getMove2()).replace("\n", " "));
                break;
            case "3":
                b.sendMessage(channel, user.attack(computer, user.getMove3()).replace("\n", " "));
                break;
            case "4":
                b.sendMessage(channel, user.attack(computer, user.getMove4()).replace("\n", " "));
                break;
            case "run":
                b.sendMessage(channel, "You got away safely!");
                return;
        }
    }

}
