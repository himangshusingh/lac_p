package org.eggzampl;

import java.util.Scanner;

public class option {
    public static void main(String[] args) {
        String choice = args[0];

        switch (choice) {
            case "--help" -> {
                System.out.println("\n\n--write:   modify existing index or create new index");
                System.out.println("           FORMAT --write <path of input files>");
                System.out.println("\n--search:  search from existing index");
                System.out.println("           FORMAT --search <word to be searched> <path of indexed files>");
                System.out.println("\nNote: Index needs to be created before searching it");
            }
            case "--write" -> {
                System.out.println("Creating Index");
                write_index.Main(args[1]);
            }
            case "--search" -> {
                try {
                    System.out.println("Reading from existing Index");
//                  read_index.Main("1");
                    read_index.Main(args[1], args[2]);
                } catch (Exception e) {
                    System.err.println("Error Occurred, Files Not Indexed Properly");
                }
            }
            default -> System.out.println("Wrong Input");
        }
    }

}
