package com.m3quiz.quizfitxers;


import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.m3quiz.quizfitxers.teclat.Teclat;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.FileReader;
import java.lang.reflect.Array;
import java.util.HashSet;
import java.util.Random;

@SpringBootApplication
public class QuizFitxersApplication {

    public static void main(String[] args) {
        String quizName = "QUIZ";
        int quizCategory;
        int amountChosenQuestions;
        boolean selectedGameModeAdvanced;
        String[] questionTitles;
        String[][] questionsOptions;
        int[] questionsCorrectOptions;

        System.out.println("---------- " + quizName + " ----------");

        // initialize values
        selectedGameModeAdvanced = getGameMode();
        amountChosenQuestions = getInputInt("How many questions do you want the quiz have? \n Choose a number between 5 and 20", 5, 20);
        String[] categories = {"Suits", "Fast And Furious"};
        quizCategory = getOptionChosenByUser("Escoje un categoria para el quiz", categories);
        int[] randomIndexes = getRandomizdNumbers(amountChosenQuestions);
        questionTitles = getQuestionsTitles(quizCategory, randomIndexes);
        questionsOptions = getQuestionOptions(quizCategory, randomIndexes);
        questionsCorrectOptions = getQuestionCorrectOptions(quizCategory, randomIndexes);

        //start quiz
        startQuiz(selectedGameModeAdvanced,questionTitles,questionsOptions,questionsCorrectOptions,amountChosenQuestions);
    }

    /**
     * start the quiz with the context of the received parameters
     * @param selectedGameModeAdvanced selected gamemode, 0-basic , 1-advanced
     * @param questionTitles array with the titles of the questions
     * @param questionsOptions array with the possible options of the questions
     * @param questionsCorrectOptions array with the index of the corret questions of the questions
     * @param amountChosenQuestions amount of questions to be displayed
     */
    private static void startQuiz(boolean selectedGameModeAdvanced, String[] questionTitles, String[][] questionsOptions,int[] questionsCorrectOptions, int amountChosenQuestions){
        double score = 0.0;
        int succesAmount = 0;
        int consecutiveErrorsAmount = 0;
        double streakMultiplier = 1.0;

        for (int i = 0; i < amountChosenQuestions; i++) {
            if(consecutiveErrorsAmount <= 2) {
                boolean isCorrect = getUserCorrectOption(questionTitles[i], questionsOptions[i], questionsCorrectOptions[i]);
                printOptionResultMessage(isCorrect);
                streakMultiplier = calculateStreakMultiplier(isCorrect, streakMultiplier);
                score = calculatScoreAmount(isCorrect, score, streakMultiplier);
                succesAmount = calculateSuccesAmount(isCorrect, succesAmount);
                consecutiveErrorsAmount = calculateConsecutiveErrorsAmount(isCorrect, consecutiveErrorsAmount, selectedGameModeAdvanced);
            }
        }

        printEndMessageScore(selectedGameModeAdvanced, score, succesAmount,amountChosenQuestions);
    }

    /**
     * print indication message depending of if the option isCorrect or not
     * @param isCorrect
     */
    private static void printOptionResultMessage(boolean isCorrect){
        if(isCorrect){
            System.out.println("\u001B[32m Correct option \u001B[39m \n");
        }else{
            System.out.println("\u001B[31m Incorrect option \u001B[39m \n");
        }
    }

    /**
     * calculate the new score amount after the question answer
     * @param isCorrect answer is correct
     * @param score actual score
     * @param streakMultiplier streak multiplier amount to add
     * @return new score value
     */
    public static double calculatScoreAmount(boolean isCorrect, double score, double streakMultiplier){
        if(isCorrect){
            return score + streakMultiplier;
        }else{
            return score;
        }
    }

    /**
     * calculate quizes succes amount
     * @param isCorrect answer is correct
     * @param succesAmount amount of successes
     * @return
     */
    private static int calculateSuccesAmount(boolean isCorrect, int succesAmount){
        if(isCorrect){
            return succesAmount + 1;
        }else{
            return succesAmount;
        }
    }

    /**
     * calulate consecutive errors
     * @param isCorrect
     * @param ConsecutiveErrorsAmount
     * @param selectedGameModeAdvanced
     * @return
     */
    private static int calculateConsecutiveErrorsAmount(boolean isCorrect, int ConsecutiveErrorsAmount, boolean selectedGameModeAdvanced){
        if(!isCorrect && selectedGameModeAdvanced){
            return ConsecutiveErrorsAmount + 1;
        }else{
            return 0;
        }
    }

    /**
     * calculate streaks multiplier
     * @param isCorrect
     * @param streakMultiplier
     * @return
     */
    private static double calculateStreakMultiplier(boolean isCorrect,  double streakMultiplier){
        if(isCorrect){
            return streakMultiplier + 0.25;
        }else{
            return 1.0;
        }
    }


    /**
     * given an array of possible answers of a question , make user select a
     * question and return this selected one
     * @param titleMessage message question title
     * @param options array of possible answers
     * @return choosen question index
     */
    private static int getOptionChosenByUser(String titleMessage, String[] options) {
        int indexChosen;
        boolean chosenCorrectly = false;

        //print title
        System.out.println(titleMessage);

        //show options
        for (int i = 0; i < options.length; i++) {
            System.out.println(i + " - " + options[i]);
        }

        indexChosen = getInputInt("", 0, options.length - 1);

        return indexChosen;
    }

    /**
     * get an input int selected by the user in a range of values
     * @param message
     * @param min
     * @param max
     * @return selected int
     */
    private static int getInputInt(String message, int min, int max) {
        boolean chosenCorrectly = false;
        // print title
        System.out.println(message);

        //get input
        int input;
        do {
            input = Teclat.llegirInt();
            if (input >= min && input <= max) {
                chosenCorrectly = true;
            } else {
                System.out.println("The input need to be between " + min + " and " + max);
            }
        } while (!chosenCorrectly);

        return input;
    }

    /**
     * make a user select an option and after check if its correct or not
     * @param questionTitle title of the question
     * @param questionsOptions array of possible answers
     * @param questionsCorrectOption correct answer index
     * @return boolean indicated if the user answered the correct option
     */
    public static boolean getUserCorrectOption(String questionTitle, String[] questionsOptions,int questionsCorrectOption){
        System.out.println("=============================");

        int takenOption = getOptionChosenByUser(questionTitle, questionsOptions);
        return isCorrect(takenOption, questionsCorrectOption);
    }

    /**
     * make user select a game mode
     * @return selected game mode by the user fasle-basic true-advanced
     */
    private static boolean getGameMode() {
        String[] modes = {
                "Easy mode, unlimited errors and % result of the quiz",
                "Advanced mode with score tracking, only 3 errors available and streaksmultipliers"
        };

        int selectedOption = getOptionChosenByUser(
                "Which game mode do you want to play?",
                modes
        );

        // select advanced or easy mode
        if (selectedOption == 0) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * compare two values and return if are the same
     * @param chosenOption
     * @param index
     * @return
     */
    public static boolean isCorrect(int chosenOption, int index) {
        return index == chosenOption;
    }


    /**
     * at the end of the quiz execution print a message indicating the user its stadistics
     * @param selectedGameModeAdvanced
     * @param score
     * @param succesAmount
     * @param amountChosenQuestions
     */
    private static void printEndMessageScore(boolean selectedGameModeAdvanced, double score, int succesAmount, int amountChosenQuestions) {
        if (selectedGameModeAdvanced) {
            System.out.println("In the hardmode you got a score of " + score + " with " + succesAmount + " succes");
        } else {
            double percentage = succesAmount * 100.0 / amountChosenQuestions;
            if (percentage < 33.3) {
                System.out.println("The user answered from  0% to 33% of the questions correctly.");
            } else if (percentage > 33.3 && percentage < 66.6) {
                System.out.println("The user answered from  34% to 66% of the questions correctly.");
            } else if (percentage > 66.6 && percentage < 99.9) {
                System.out.println("The user answered from  67% to 99% of the questions correctly.");
            } else if (percentage == 100) {
                System.out.println("The user answered 100% of the questions correctly.");
            }
        }
    }

    /**
     * genereate an amount of nmbers between 0 and the passed parameter, store them in a array and randomize its order
     * @param amount maxium number
     * @return array with randomized numbers
     */
    private static int[] getRandomizdNumbers(int amount) {
        int[] randomized = new int[amount];
        Random random = new Random();
        HashSet<Integer> usedNumbers = new HashSet<>();

        for (int i = 0; i < amount; i++) {
            int randomNumber;
            boolean valid = false;
            do {
                randomNumber = random.nextInt(20);
                if (!usedNumbers.contains(randomNumber)) {
                    usedNumbers.add(randomNumber);
                    valid = true;
                }
            } while (!valid);

            randomized[i] = randomNumber;
        }

        return randomized;
    }

    /**
     * given an arrray of values randomIndexes and an array of values arrrayValues, create an array
     * with stored values where the index and order depends of the one in randomIndexes and its the sam of it
     * but with the values of arrayValues
     * @param randomIndexes
     * @param arrayValues
     * @return
     */
    public static int[] getValuesFromRandomIndexInt(int[] randomIndexes, int[] arrayValues){

        int[] unorderedValuesrray = new int[randomIndexes.length];

        for(int i = 0; i < randomIndexes.length; i++){
            unorderedValuesrray[i] = arrayValues[randomIndexes[i]];
        }
        return unorderedValuesrray;
    }
    /**
     * given an arrray of values randomIndexes and an array of values arrrayValues, create an array
     * with stored values where the index and order depends of the one in randomIndexes and its the sam of it
     * but with the values of arrayValues
     * @param randomIndexes
     * @param arrayValues
     * @return
     */
    public static <T> T[] getValuesFromRandomIndex(int[] randomIndexes, T[] arrayValues){

        @SuppressWarnings("unchecked")
        T[] unorderedValuesrray = (T[]) Array.newInstance(arrayValues.getClass().getComponentType(), randomIndexes.length);

        for(int i = 0; i < randomIndexes.length; i++){
            unorderedValuesrray[i] = arrayValues[randomIndexes[i]];
        }
        return unorderedValuesrray;
    }

    /**
     * get array of a titlles given a selected category and with the ids(index) of the questions to take
     * @param quizCategory selected category
     * @param randomQuetionsIndex array of questions index to return for the selected category
     * @return
     */
    private static String[] getQuestionsTitles(int quizCategory, int[] randomQuetionsIndex) {
        String jsonFilePath = "./data/data.json";

        try {
            FileReader fileReader = new FileReader(jsonFilePath);

            JsonParser jsonparser = new JsonParser();
            // Parse the JSON using JsonParser
            JsonElement jsonElement = jsonparser.parse(fileReader);

            // Get the JsonObject from the parsed JSON
            JsonObject jsonObject = jsonElement.getAsJsonObject();

            // Get the questionTitles JsonObject
            JsonObject questionTitlesObject = jsonObject.getAsJsonObject("questionTitles");

            // Get the questionTitles array for index 0
            JsonArray questionTitlesArray = questionTitlesObject.getAsJsonArray(Integer.toString(quizCategory));

            // Convert the JsonArray to an int[]
            String[] titlesArray = new String[questionTitlesArray.size()];
            for (int i = 0; i < questionTitlesArray.size(); i++) {
                titlesArray[i] = questionTitlesArray.get(i).getAsString();
            }

            return getValuesFromRandomIndex(randomQuetionsIndex,titlesArray);
        }catch(Exception err){

        }

        return null;
    }

    /**
     * get array of a answers given a selected category and with the ids(index) of the questions to take
     * @param quizCategory selected category
     * @param randomQuetionsIndex array of questions index to return for the selected category
     * @return
     */
    private static String[][] getQuestionOptions(int quizCategory, int[] randomQuetionsIndex) {
        String jsonFilePath = "./data/data.json";

        try {
            FileReader fileReader = new FileReader(jsonFilePath);

            JsonParser jsonparser = new JsonParser();
            // Parse the JSON using JsonParser
            JsonElement jsonElement = jsonparser.parse(fileReader);

            // Get the JsonObject from the parsed JSON
            JsonObject jsonObject = jsonElement.getAsJsonObject();

            // Get the questionTitles JsonObject
            JsonObject questionTitlesObject = jsonObject.getAsJsonObject("questionTitles");

            // Get the questionTitles array for index 0
            JsonArray questionArray = questionTitlesObject.getAsJsonArray(Integer.toString(quizCategory));

            String[][] titlesArray = new String[questionArray.size()][4];
            for (int i = 0; i < questionArray.size(); i++) {
                JsonArray innerArray = questionArray.get(i).getAsJsonArray();
                for (int j = 0; j < innerArray.size(); j++) {
                    titlesArray[i][j] = innerArray.get(j).getAsString();
                }
            }
            return getValuesFromRandomIndex(randomQuetionsIndex,titlesArray);
        }catch(Exception err){

        }
        return null;
    }

    /**
     * get array of a answers given a selected category and with the ids(index) of the questions to take
     * @param quizCategory selected category
     * @param randomQuetionsIndex array of questions index to return for the selected category
     * @return
     */
    private static int[] getQuestionCorrectOptions(int quizCategory,int[] randomQuetionsIndex) {
        String jsonFilePath = "./data/data.json";

        try {
            FileReader fileReader = new FileReader(jsonFilePath);

            JsonParser jsonparser = new JsonParser();
            // Parse the JSON using JsonParser
            JsonElement jsonElement = jsonparser.parse(fileReader);

            // Get the JsonObject from the parsed JSON
            JsonObject jsonObject = jsonElement.getAsJsonObject();

            // Get the questionTitles JsonObject
            JsonObject questionTitlesObject = jsonObject.getAsJsonObject("questionTitles");

            // Get the questionTitles array for index 0
            JsonArray questionTitlesArray = questionTitlesObject.getAsJsonArray(Integer.toString(quizCategory));

            // Convert the JsonArray to an int[]
            int[] intArray = new int[questionTitlesArray.size()];
            for (int i = 0; i < questionTitlesArray.size(); i++) {
                intArray[i] = questionTitlesArray.get(i).getAsInt();
            }

            return intArray;
        }catch(Exception err){

        }

        return null;
    }

}
