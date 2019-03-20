package pt.ulisboa.tecnico.meic.sec.gui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * A class for presenting a menu
 */
public class MenuUI {
    /** List of entry **/
    private ArrayList<String> entries;

    /** Menu title **/
    private String title;

    public MenuUI() {
        this.title = "";
        entries = new ArrayList<String>();
    }

    public MenuUI(String title) {
        this.title = title;
        entries = new ArrayList<String>();
    }

    public void addEntry(String entry) {
        entries.add(entry);
    }

    public int display() {
        System.out.print("\033[H\033[2J");
        System.out.flush();

        System.out.println(title);

        for (int i = 0; i < title.length(); i++)
            System.out.print("-");
        System.out.println();

        int i = 1;
        for (String entry : entries) {
            System.out.println(i + " -> " + entry);
            i++;
        }

        String inputString = null;
        int option = -1;
        while (option == -1) {

            System.out.println();
            System.out.print(BoxUI.WHITE_BOLD + "Your choice is: " + BoxUI.RESET);

            System.out.flush();

            try {
                BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
                inputString = bufferRead.readLine();
                option = Integer.parseInt(inputString);

                if (option <= 0 || option > entries.size()) {
                    option = -1;
                    System.out.println(BoxUI.RED_BOLD + "Ops! Wrong number. Try again!" + BoxUI.RESET);
                    bufferRead.readLine();
                }


            } catch (IOException ex) {
                ex.printStackTrace();
            }
            catch (Exception e) {
                //Asks again
            }
        }
        return option;
    }

    public String displayOption() {
        int option = display();
        return entries.get(option - 1);
    }
}
