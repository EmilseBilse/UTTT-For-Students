package dk.easv.bll.bot;

import dk.easv.bll.game.GameManager;
import dk.easv.bll.game.GameState;
import dk.easv.bll.game.IGameState;
import dk.easv.bll.move.IMove;
import dk.easv.bll.move.Move;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class RyeBotTry2 implements IBot {

    private static final String BOTNAME = "Rye Bot";
    Random random = new Random();

    @Override
    public IMove doMove(IGameState state) {
        return calcMove(state);
    }

    public IMove calcMove(IGameState state) {
        int thisPlayer = getCurrentPlayer(state);
        int oppPlayer=(thisPlayer +1) %2;
        List<IMove> winMoves = getWinningMoves(state, thisPlayer);

        //if the bot is the starting player it goes for the middle
        if(state.getMoveNumber() == 0) {
            return(new Move(4,4));
        }

        //checks local Wins
        if(!winMoves.isEmpty()) {
            for(IMove currentMove: winMoves) {
                GameState gs = new GameState(state);
                GameManager gm = new GameManager(gs);
                gm.updateGame(currentMove);
                //If is able to win globally do it
                if(gm.getGameOver().equals(GameManager.GameOverState.Win)) {
                    return currentMove;
                }
                //check if a local win results in the opponent being able to win globally next time, if not do it
                List<IMove> oppWinMoves = getWinningMoves(gm.getCurrentState(), oppPlayer);
                for(IMove currentOppMove: oppWinMoves) {
                    GameState gs1 = new GameState(gm.getCurrentState());
                    GameManager gm1 = new GameManager(gs1);
                    gm1.updateGame(currentOppMove);
                     if(!gm1.getGameOver().equals(GameManager.GameOverState.Win)) {
                         return currentMove;
                     }
                }
            }
        }

        //checks local blocking
        else{
            List<IMove> oppWinMoves = getWinningMoves(state, oppPlayer);
            if(!oppWinMoves.isEmpty()) {
                for(IMove currentOppMove: oppWinMoves) {
                    GameState gs = new GameState(state);
                    GameManager gm = new GameManager(gs);
                    gm.updateGame(currentOppMove);
                    //checks if the local block will result in the opponent winning globally, if not returns the move
                    if(!gm.getGameOver().equals(GameManager.GameOverState.Win)){
                        return currentOppMove;
                    }
                }
            }
        }

        List<IMove> prefMoves = getPreferredMoves(state);
        if(!prefMoves.isEmpty()){
            return prefMoves.get(random.nextInt(prefMoves.size()));
        }

        return state.getField().getAvailableMoves().get(random.nextInt(state.getField().getAvailableMoves().size()));
    }

    private List<IMove> getPreferredMoves(IGameState state) {
        List<IMove> availableMoves = state.getField().getAvailableMoves();
        List<IMove> returnMoves = new ArrayList<>();
        int thisPlayer = getCurrentPlayer(state);
        int oppPlayer=(thisPlayer +1) %2;

        List<IMove> macroOuterMiddleMoves = new ArrayList<>();
        macroOuterMiddleMoves.add(new Move(0, 1));
        macroOuterMiddleMoves.add(new Move(2, 1));
        macroOuterMiddleMoves.add(new Move(1, 0));
        macroOuterMiddleMoves.add(new Move(1, 2));

        List<IMove> outerMiddleMoves = upscaleMoves(macroOuterMiddleMoves);


        List<IMove> macroCornerMoves = new ArrayList<>();
        macroCornerMoves.add(new Move(0, 0));
        macroCornerMoves.add(new Move(2, 0));
        macroCornerMoves.add(new Move(2, 0));
        macroCornerMoves.add(new Move(2,2));
        List<IMove> cornerMoves = upscaleMoves(macroCornerMoves);

        List<IMove> middleMoves =new ArrayList<>();
        middleMoves.add(new Move(1,1));
        middleMoves.add(new Move(4, 1));
        middleMoves.add(new Move(7, 1));
        middleMoves.add(new Move(1,4));
        middleMoves.add(new Move(1,7));
        middleMoves.add(new Move(4,4));
        middleMoves.add(new Move(4,7));
        middleMoves.add(new Move(7,4));
        middleMoves.add(new Move(7,7));

        // removes moves that let the opponent pick from the entire board next time (skal nok laves bedre)
        List<IMove> smartMoves = new ArrayList<>();
        for (IMove move: availableMoves) {
            GameState gs = new GameState(state);
            GameManager gm = new GameManager(gs);
            gm.updateGame(move);
            if(gm.getCurrentState().getField().getAvailableMoves().size()<=9){
                smartMoves.add(move);
            }
        }
        System.out.print(smartMoves.size());
        if(smartMoves.isEmpty()){
            smartMoves = availableMoves;
        }

        //adds OuterMiddleMoves
        addMoves(state, returnMoves, oppPlayer, outerMiddleMoves, smartMoves);
        //adds CornerMoves
        if(returnMoves.isEmpty()) {
            addMoves(state, returnMoves, oppPlayer, cornerMoves, smartMoves);
        }
        //adds middleMoves
        if(returnMoves.isEmpty()) {
            addMoves(state, returnMoves, oppPlayer, middleMoves, smartMoves);
        }
        //System.out.print(returnMoves.size());
        if(returnMoves.isEmpty()) {
            returnMoves = smartMoves;
        }
        return returnMoves;

    }

    private void addMoves(IGameState state, List<IMove> returnMoves, int oppPlayer, List<IMove> outerMiddleMoves, List<IMove> smartMoves) {
        for(IMove move:smartMoves) {
            for(IMove outerMiddleMove: outerMiddleMoves) {
                if(imovesMatching(move,outerMiddleMove)&&!returnMoves.contains(move)){
                    GameState gs = new GameState(state);
                    GameManager gm = new GameManager(gs);
                    gm.updateGame(move);
                    if(getWinningMoves(gm.getCurrentState(), oppPlayer).isEmpty()){
                        returnMoves.add(move);
                    }
                }
            }
        }
    }

    private List<IMove> upscaleMoves (List<IMove> moves) {
        List<IMove> returnMoves = new ArrayList<>();
        for(IMove currentMove: moves) {
            returnMoves.add(currentMove);
            int i = 3;
            returnMoves.add(new Move(currentMove.getX(), currentMove.getY()+i));
            returnMoves.add(new Move(currentMove.getX(), currentMove.getY()+(i*2)));
            returnMoves.add(new Move(currentMove.getX()+i, currentMove.getY()));
            returnMoves.add(new Move(currentMove.getX()+(i*2), currentMove.getY()));
            returnMoves.add(new Move(currentMove.getX()+i, currentMove.getY()+i));
            returnMoves.add(new Move(currentMove.getX()+i, currentMove.getY()+(i*2)));
            returnMoves.add(new Move(currentMove.getX()+(i*2), currentMove.getY()+i));
            returnMoves.add(new Move(currentMove.getX()+(i*2), currentMove.getY()+(i*2)));
        }
        return returnMoves;
    }

    private boolean imovesMatching (IMove move1, IMove move2){
        if(move1.getX() == move2.getX() && move2.getX()== move2.getX()) {
            return true;
        }
        return false;
    }

    private boolean isWinningMove(String[][] board, IMove move, String player){
        boolean isRowWin = true;
        // Row checking
        int startX = move.getX()-(move.getX()%3);
        int endX = startX + 2;
        for (int x = startX; x <= endX; x++) {
            if(x!=move.getX())
                if(!board[x][move.getY()].equals(player))
                    isRowWin = false;
        }

        boolean isColumnWin=true;
        // Column checking
        int startY = move.getY()-(move.getY()%3);
        int endY = startY + 2;
        for (int y = startY; y <= endY; y++) {
            if(y!=move.getY())
                if(!board[move.getX()][y].equals(player))
                    isColumnWin = false;
        }


        boolean isDiagWin = true;

        // Diagonal checking left-top to right-bottom

        for(int i = 0; i<=2; i++) {
            int newX = startX+i;
            int newY = startY+i;
            if(!(move.getX()==newX && move.getY()==newY))
                if(!board[newX][newY].equals(player))
                    isDiagWin=false;
        }

        boolean isOppositeDiagWin = true;
        // Diagonal checking left-bottom to right-top
        if(!(move.getX()==startX && move.getY()==startY+2))
            if(!board[startX][startY+2].equals(player))
                isOppositeDiagWin=false;
        if(!(move.getX()==startX+1 && move.getY()==startY+1))
            if(!board[startX+1][startY+1].equals(player))
                isOppositeDiagWin=false;
        if(!(move.getX()==startX+2 && move.getY()==startY))
            if(!board[startX+2][startY].equals(player))
                isOppositeDiagWin=false;

        return isColumnWin || isDiagWin || isOppositeDiagWin || isRowWin;
    }

    // Compile a list of all available winning moves
    private List<IMove> getWinningMoves(IGameState state, int chosenPlayer){
        String player = String.valueOf(chosenPlayer);

        List<IMove> avail = state.getField().getAvailableMoves();

        List<IMove> winningMoves = new ArrayList<>();
        for (IMove move:avail) {
            if(isWinningMove(state.getField().getBoard(),move,player))
                winningMoves.add(move);
        }
        return winningMoves;
    }

    private int getCurrentPlayer(IGameState state){
        return state.getMoveNumber()%2;
    }

    @Override
    public String getBotName() {
        return BOTNAME;
    }
}
