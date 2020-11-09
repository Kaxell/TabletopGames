package games.catan;

import core.AbstractGameState;
import core.AbstractForwardModel;
import core.actions.AbstractAction;
import core.actions.DoNothing;
import core.components.*;
import games.catan.actions.BuildRoad;
import games.catan.actions.BuildSettlement;

import java.util.*;

import static core.CoreConstants.playerHandHash;

public class CatanForwardModel extends AbstractForwardModel {
    private int rollCounter;
    CatanParameters params;
    int nPlayers;

    public CatanForwardModel(){}

    public CatanForwardModel(CatanParameters pp, int nPlayers) {
        this.params = pp;
        this.nPlayers = nPlayers;
    }

    @Override
    protected void _setup(AbstractGameState firstState) {
        // todo set everything to the state
        Random rnd = new Random(firstState.getGameParameters().getRandomSeed());

        CatanGameState state = (CatanGameState) firstState;
        CatanParameters params = (CatanParameters)state.getGameParameters();
        // data is read in from JSON it has all the cards, tokens and counters
        CatanData data = state.getData();

        state.setBoard(generateBoard(params));
        state.areas = new HashMap<>();

        // Setup areas
        for (int i = 0; i < state.getNPlayers(); i++) {
            Area playerArea = new Area(i, "Player Area");
            Deck<Card> playerHand = new Deck<>("Player Hand");
            playerHand.setOwnerId(i);
            playerArea.putComponent(playerHandHash, playerHand);
            state.areas.put(i, playerArea);
        }

        // TODO (oh) initialise decks

        // Initialize the game area
        Area gameArea = new Area(-1, "Game Area");
        state.areas.put(-1, gameArea);

        state.addComponents();

    }

    @Override
    protected void _next(AbstractGameState currentState, AbstractAction action) {
        CatanGameState gs = (CatanGameState) currentState;
        // todo (mb) make sure that reactions are handled correctly (trading)
//        if (((CatanTurnOrder)gs.getTurnOrder()).reactionsFinished()){
//            gs.setMainGamePhase();
//        }
        gs.setRollValue(rollDice(gs.getGameParameters().getRandomSeed()));
        action.execute(gs);

        // end player's turn
        gs.getTurnOrder().endPlayerTurn(gs);

    }

    @Override
    protected List<AbstractAction> _computeAvailableActions(AbstractGameState gameState) {
        ArrayList<AbstractAction> actions = new ArrayList<>();
        CatanGameState gs = (CatanGameState)gameState;
        if (gs.getGamePhase() == CatanGameState.CatanGamePhase.Setup){
            System.out.println("setting settlements with roads");
            // TODO (mb) in initial phase each player places 2 roads and 2 settlements on the board
        }
        if (gs.getGamePhase() == AbstractGameState.DefaultGamePhase.Main){
//            actions.add(new DoNothing());
        }

        // todo (mb) instead of random determine where to build settlement
        Random rnd = new Random();
        int row = rnd.nextInt(7);
        int col = rnd.nextInt(7);
        int edge = rnd.nextInt(6);
        actions.add(new BuildSettlement(row, col, edge, gameState.getCurrentPlayer()));

        // todo (mb) instead of random determine where the player can put roads
        row = rnd.nextInt(7);
        col = rnd.nextInt(7);
        edge = rnd.nextInt(6);
        actions.add(new BuildRoad(row, col, edge, gameState.getCurrentPlayer()));


        // todo (mb) some notes on rules
        // 1, victory cards may only be played when player has 10+ points, can be in the same turn when drawn
        // 2, other dev cards cannot be played on the same turn when they are drawn and only 1 card per turn is playable
        // 3, distance rule - each settlement requires 2 edge distance from other settlements
        // 4, trade is a negotiation in the game - should player send an offer to all other players?
        return actions;
    }

    @Override
    protected AbstractForwardModel _copy() {
        return null;
    }

    private CatanTile[][] generateBoard(CatanParameters params){
        // Shuffle the tile types
        ArrayList<CatanParameters.TileType> tileList = new ArrayList<>();
        for (Map.Entry tileCount : params.tileCounts.entrySet()){
            for (int i = 0; i < (int)tileCount.getValue(); i++) {
                tileList.add((CatanParameters.TileType)tileCount.getKey());
            }
        }
        // Shuffle number tokens
        ArrayList<Integer> numberList = new ArrayList<>();
        for (Map.Entry numberCount : params.numberTokens.entrySet()){
            for (int i = 0; i < (int)numberCount.getValue(); i++) {
                numberList.add((Integer)numberCount.getKey());
            }
        }
        // shuffle collections so we get randomized tiles and tokens on them
        Collections.shuffle(tileList);
        Collections.shuffle(numberList);

        CatanTile[][] board = new CatanTile[7][7];
        int mid_x = board.length/2;
        int mid_y = board[0].length/2;

        CatanTile midTile = new CatanTile(mid_x, mid_y);
        midTile.setTileType(CatanParameters.TileType.DESERT);

        for (int x = 0; x < board.length; x++){
            for (int y = 0; y < board[x].length; y++){
                CatanTile tile = new CatanTile(x, y);
                // mid_x should be the same as the distance
                if (midTile.distance(tile) >= mid_x){
                    tile.setTileType(CatanParameters.TileType.SEA);
                }
                else if (x == mid_x && y == mid_y){
                    tile = midTile;
                    tile.placeRobber();
                }
                else if (tileList.size() > 0) {
                    tile.setTileType(tileList.remove(0));
                    tile.setNumber(numberList.remove(0));
                }
                board[x][y] = tile;
            }
        }

        // todo traverse through the board and set vertices and edges
        for (int x = 0; x < board.length; x++) {
            for (int y = 0; y < board[x].length; y++) {
                CatanTile tile = board[x][y];
                System.out.println("tile " + tile);
            }
        }

        return board;
    }

    public int rollDice(long seed){
        Random r1 = new Random(seed + rollCounter);
        rollCounter += 1;
        Random r2 = new Random(seed + rollCounter);
        rollCounter += 1;
        int num1 = r1.nextInt(6);
        int num2 = r2.nextInt(6);

        return num1 + num2 + 2;
    }

//    public void setVertices(CatanTile[][] board, int row, int column, int vertex, int value){
//        // Sets the neighbours of a given vertex to a given value
//        if (vertex == 0){
//            board[row][column].settlements[vertex] = value;
//            board[row][column-1].settlements[vertex] = value;
//            board[row+1][column].settlements[vertex] = value;
//        }
//    }
}
