package game.reoord;

import game.state.difficulty.DifficultyMode;
import user.User;

import java.util.HashMap;
import java.util.Map;

public class GameRecord {

    private int attemptCnt; //게임 시도 횟수
    private final DifficultyMode difficultyMode;

    public GameRecord(int gameNumber,  DifficultyMode difficultyMode) {
        this.attemptCnt = 0;
        this.difficultyMode = difficultyMode;
    }

    public void increaseAttemptCnt() {
        this.attemptCnt++;
    }

    public DifficultyMode getDifficultyMode() {
        return difficultyMode;
    }

    public int getAttemptCnt() {
        return attemptCnt;
    }
}
