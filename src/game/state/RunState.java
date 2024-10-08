package game.state;

import ex.GameInitializationException;
import ex.InvalidInputException;
import game.BaseballGame;
import game.record.GameRecord;
import game.difficulty.DifficultyMode;
import game.logic.BaseballGameLogic;
import game.state.menu.MenuState;
import user.User;
import util.CustomDesign;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * 숫자 야구 게임의 실행 상태를 관리하는 클래스입니다
 * NumberBaseballLogic을 사용해 게임의 실제 플레이 로직을 처리합니다
 */
public class RunState implements GameState {

    private final BaseballGameLogic baseballGameLogic;
    private static final String INPUT_PROMPT = " 자리 수를 입력해주세요: ";

    /**
     * RunState의 생성자입니다
     *
     * @param difficultyMode 게임의 난이도 모드
     */
    public RunState(DifficultyMode difficultyMode){
        this.baseballGameLogic = new BaseballGameLogic(difficultyMode.getLen());
    }

    /**
     * 게임을 실행하고 처리합니다
     * 게임을 초기화, 플레이, 종료 과정을 관리합니다
     *
     * @param baseballGame 숫자 야구 게임 객체
     * @param sc Scanner 객체
     */
    @Override
    public void handle(BaseballGame baseballGame, Scanner sc) {
        User currentUser = baseballGame.getCurrentUser();
        GameRecord gameRecord = null;
        boolean isGameInitialized = false;
        try {
            gameRecord = initialize(currentUser);
            isGameInitialized = true;
            playGame(sc, gameRecord);
        }catch(GameInitializationException e){
            CustomDesign.printExceptionMessage(e.getMessage());
        }finally {
            if(isGameInitialized)  finishGame(baseballGame, baseballGame.getCurrentUser(), gameRecord);
            else baseballGame.nextStep(MenuState.getInstance());
        }
    }

    /**
     * 실제 게임 플레이를 처리합니다
     * 사용자 입력을 받고 정답을 맞출 때까지 반복합니다
     *
     * @param sc Scanner 객체
     * @param gameRecord 현재 게임의 기록 객체
     */
    private void playGame(Scanner sc, GameRecord gameRecord){
        while(true){
            //입력 실패하면 재시작
            gameRecord.increaseAttemptCnt();
            try {
                if (!processUserInput(sc)) {
                    continue;
                }
            }catch(InvalidInputException e){
                CustomDesign.printExceptionMessage(e.getMessage());
                continue;
            }
            //성공하면 반복문 종료
            gameRecord.setFinished(true);
            gameRecord.setFinishedDate(LocalDateTime.now());
            break;
        }
    }

    /**
     * 게임을 종료하고 결과를 처리합니다
     *
     * @param baseballGame 숫자 야구 게임 객체
     * @param user 현재 사용자
     * @param gameRecord 완료된 게임의 기록 객체
     */
    private void finishGame(BaseballGame baseballGame,  User user, GameRecord gameRecord){
        user.addToGameRecordList(gameRecord);
        //다시 메뉴로 전환
        baseballGame.nextStep(MenuState.getInstance());
    }

    /**
     * 새로운 GameRecord 객체를 생성하고 랜덤 숫자를 생성합니다
     *
     * @param user 현재 사용자
     * @return 초기화된 GameRecord 객체, 초기화 실패시 null
     * @throws GameInitializationException 게임 중 초기화 관련 오류 발생 시 throw
     */
    private GameRecord initialize(User user) throws GameInitializationException{
        try {
            GameRecord gameRecord = new GameRecord(user.getGameNumber(), baseballGameLogic.getMode());
            baseballGameLogic.generateRandomNumber();
            return gameRecord;
        }catch(NoSuchElementException e){
            throw new GameInitializationException("게임 레코드 초기화 중 오류가 발생했습니다: "+e.getMessage(), e);
        }
    }


    /**
     * 사용자 입력을 처리하고 검증합니다
     *
     * @param sc Scanner 객체
     * @return 정답을 맞췄으면 true, 그렇지 않으면 false
     */
    private boolean processUserInput(Scanner sc){
        System.out.print(CustomDesign.ANSI_PINK + baseballGameLogic.getLen() + INPUT_PROMPT + CustomDesign.ANSI_RESET);
        String input = sc.nextLine();
        if(input.isEmpty())
            throw new InvalidInputException("숫자를 입력해주세요");
        return baseballGameLogic.validateAnswer(input);
    }

}
