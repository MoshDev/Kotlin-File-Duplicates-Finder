package com.ersan.space;

/**
 * Created by M.Ersan on 10/12/2017.
 */
public class Main {

    public static void main(String[] args) {
//
//
//        if (args == null || args.length == 0) {
//            throw new IllegalArgumentException("Please provide the directory path to scan");
//        }
//

        String inputDir = "/Users/ersan/Android/Bitbucket/sample_test";
        Starter starter = new Starter(new Args(inputDir));
        starter.deletedDuplicates();


    }
}