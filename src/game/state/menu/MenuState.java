package game.state.menu;

import ex.InvalidInputException;
import game.BaseballGame;
import game.state.*;
import game.difficulty.DifficultyMode;
import game.state.ranking.RankingState;
import util.CustomDesign;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.function.BiConsumer;
import java.util.function.Function;
/**
 * 게임의 메뉴 상태를 관리하는 클래스입니다
 * 메인 메뉴와 난이도 선택 메뉴를 처리하고, 사용자의 입력에 따라 적절한 게임 상태로 전환합니다
 */
public class MenuState implements GameState {
    private static MenuState menuState;
    private final Map<Integer, BiConsumer<BaseballGame, Scanner>> optionStates;
    private MenuStatus currentStatus;

    private static final int MIN_OPTION = 1;
    private static final int MAX_OPTION = 4;

    private static final String INPUT_MISMATCH_EXCEPTION_MESSAGE = "유효한 옵션 번호를 입력해주세요";


    /**
     * 기본 생성자입니다
     * private으로 선언되어 외부에서 직접 인스턴스를 생성할 수 없습니다
     * 메뉴 옵션에 대한 동작을 초기화하고, 초기 상태를 메인 메뉴로 설정합니다
     */
    private MenuState(){
        this.optionStates = new HashMap<>();
        optionStates.put(1, this::validateDifficultyModeInput);
        optionStates.put(2, (game, sc) -> game.nextStep(RecordState.getInstance()));
        optionStates.put(3, (game, sc) -> game.nextStep(RankingState.getInstance()));
        optionStates.put(4, (game, sc) -> game.nextStep(LogoutState.getInstance()));
        currentStatus = MenuStatus.MAIN_MENU;
    }
    public static synchronized MenuState getInstance(){
        if(menuState ==null){
           menuState = new MenuState();
        }
        return menuState;
    }

    /**
     * 사용자 입력을 처리하고 게임 상태를 변경합니다.
     * 이 메서드는 메인 메뉴를 표시하고, 사용자의 선택에 따라 적절한 동작을 수행합니다.
     *
     * @param baseballGame 현재의 야구 게임 인스턴스
     */
    public void handle(BaseballGame baseballGame, Scanner sc){
        resetToMainMenu();
        int option = validateMainMenuInput(sc);
        optionStates.getOrDefault(option, (game, s) -> CustomDesign.printExceptionMessage("유효한 옵션 번호를 입력해주세요"))
                        .accept(baseballGame, sc);
    }

    /**
     * 메뉴 상태를 메인 메뉴로 재설정합니다
     */
    private void resetToMainMenu() {
        currentStatus = MenuStatus.MAIN_MENU;
    }

    /**
     * 현재 메뉴 상태에 따라 적절한 메뉴 옵션을 출력합니다.
     */
    private void printAvailableMenuOptions() {
        switch (currentStatus) {
            case MAIN_MENU:
                CustomDesign.printMainMenu();
                break;
            case DIFFICULTY_SELECTION:
                CustomDesign.printDifficultyMenu();
                break;
        }
    }


    /**
     * 메인 메뉴 입력을 검증합니다
     *
     * @param sc 사용자 입력을 받기 위한 Scanner 객체
     * @return 유효한 메인 메뉴 옵션 번호
     */
    private int validateMainMenuInput(Scanner sc){
        return validateInput(
                option -> {
                    if (option < MIN_OPTION || option > MAX_OPTION) throw new InvalidInputException("유효한 옵션 번호를 입력해주세요");
                    return option;
                }
                ,sc
        );
    }

    /**
     * 난이도 모드 입력을 검증하고 게임 상태를 변경합니다
     *
     * @param baseballGame 현재의 야구 게임 인스턴스
     * @param sc 사용자 입력을 받기 위한 Scanner 객체
     */
    private void validateDifficultyModeInput(BaseballGame baseballGame, Scanner sc){
        currentStatus = MenuStatus.DIFFICULTY_SELECTION;

        DifficultyMode difficultyMode = validateInput(DifficultyMode::findByOption, sc);

        baseballGame.nextStep(new RunState(difficultyMode));
    }


    /**
     * 사용자 입력을 검증합니다
     * 다양한 타입의 입력 검증에 사용될 수 있습니다
     *
     * @param <T> 반환될 값의 타입
     * @param validator 입력을 검증하고 변환하는 함수
     * @param sc 사용자 입력을 받기 위한 Scanner 객체
     * @return 검증된 입력 값
     */
    private <T> T validateInput(Function<Integer, T> validator, Scanner sc){
        while(true){
            printAvailableMenuOptions();
            String input = sc.nextLine().trim();

            if (input.isEmpty()) {
                CustomDesign.printExceptionMessage("값을 입력해주세요");
                continue;
            }

            try{
                int option = Integer.parseInt(input);
                return validator.apply(option);
            }catch(NumberFormatException e){
                CustomDesign.printExceptionMessage(INPUT_MISMATCH_EXCEPTION_MESSAGE);
            }catch (InvalidInputException e){
                CustomDesign.printExceptionMessage(e.getMessage());
            }
        }
    }

}
