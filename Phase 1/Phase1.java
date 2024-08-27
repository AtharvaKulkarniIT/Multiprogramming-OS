package org.multiprogramming_os;

import java.io.File;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.Scanner;

public class Phase1
{
    private static final char[][] M = new char[100][4];
    private static int IC;
    private static final char[] IR = new char[4];
    private static boolean C;
    private static final char[] R = new char[4];
    private static int indexForM;
    private static String buffer;
    private static FileWriter filewriter;
    private static boolean flag1;
    private static boolean flag2;
    private static Scanner scanner;

    private static void Init()
    {
        for(char[] rowInMemory : M)
            Arrays.fill(rowInMemory, '-');

        Arrays.fill(IR, '-');
        Arrays.fill(R, '-');

        IC = 0;
        C = false;
        buffer = null;

        flag1 = false;
        flag2 = false;
        indexForM = 0;
    }

    private static void Read()
    {
        IR[3] = '0';
        buffer = scanner.nextLine();
        char[] array = buffer.toCharArray();
        int indexForArray = 0;
        boolean flag = false;
        for(int i = Integer.parseInt("" + IR[2] + IR[3]); i < (Integer.parseInt("" + IR[2] + IR[3])) + 10; i++)
        {
            for(int j = 0; j < 4; j++)
            {
                M[i][j] = array[indexForArray];
                indexForArray++;
                if(indexForArray >= array.length)
                {
                    flag = true;
                    break;
                }
            }
            if(flag)
                break;
        }
    }

    private static void Write()
    {
        IR[3] = '0';
        StringBuilder sb = new StringBuilder();
        for(int i = Integer.parseInt("" + IR[2] + IR[3]); i < (Integer.parseInt("" + IR[2] + IR[3])) + 10; i++)
        {
            for(int j = 0; j < 4; j++)
            {
                if(M[i][j] == '-')
                    sb.append(" ");
                else
                    sb.append(M[i][j]);
            }
        }
        sb.append("\n");
        try
        {
            filewriter.write(sb.toString());
            filewriter.flush(); // After we write using filewriter.write(), if there is any data remaining in buffer, filewriter.flush() forces to write the remaining contents of buffer to file
        }
        catch(Exception e)
        {
            System.out.println("Error: Unable to write to the file. " + e.getMessage());
        }
    }

    private static void Terminate()
    {
        try
        {
            filewriter.write("\n\n\n");
            filewriter.flush();
        }
        catch(Exception e)
        {
            System.out.println("Error: Unable to terminate the file writing. " + e.getMessage());
            System.exit(1);
        }
    }

    private static void MOS(int SI)
    {
        switch(SI)
        {
            case 1:
                Phase1.Read();
                break;
            case 2:
                Phase1.Write();
                break;
            case 3:
                Phase1.Terminate();
                break;
        }
    }

    private static void ExecuteUserProgram()
    {
        while(true)
        {
            int SI;
            for (int i = 0; i < 4; i++)
                IR[i] = M[IC][i];
            IC++;
            String opcode;

            if (IR[1] == ' ')
                opcode = "" + IR[0];
            else
                opcode = "" + IR[0] + IR[1];

            int operand = 0;
            if (IR[1] != ' ')
            {
                String s = "" + IR[2] + IR[3];
                operand = Integer.parseInt(s);
            }
            switch (opcode)
            {
                case "LR":
                    for (int i = 0; i < 4; i++)
                        R[i] = M[operand][i];
                    break;
                case "SR":
                    for (int i = 0; i < 4; i++)
                        M[operand][i] = R[i];
                    break;
                case "CR":
                    boolean flag = true;
                    for (int i = 0; i < 4; i++)
                    {
                        if (M[operand][i] != R[i])
                        {
                            flag = false;
                            break;
                        }
                    }
                    C = flag;
                    break;
                case "BT":
                    if (C)
                        IC = Integer.parseInt((("" + IR[2]) + IR[3]));
                    break;
                case "GD":
                    SI = 1;
                    Phase1.MOS(SI);
                    break;
                case "PD":
                    SI = 2;
                    Phase1.MOS(SI);
                    break;
                case "H":
                    SI = 3;
                    Phase1.MOS(SI);
                    return;
            }
        }
    }

    private static void StartExecution()
    {
        IC = 0;
        Phase1.ExecuteUserProgram();
    }

    private static void Load()
    {
        while(scanner.hasNextLine())
        {
            buffer = scanner.nextLine();
            if (buffer.length() >= 4)
            {
                String first4char = buffer.substring(0, 4);
                if (first4char.equals("$AMJ"))
                {
                    flag1 = true;
                }
                else if (first4char.equals("$DTA"))
                {
                    Phase1.StartExecution();
                    flag1 = true;
                }
                else if (first4char.equals("$END"))
                {
                    for(int k = 0; k < M.length; k++)
                        System.out.println(k + ":" + Arrays.toString(M[k]));
                    System.out.println("-".repeat(83) + "Job Over" + "-".repeat(83));
                    Phase1.Init();
                    flag1 = true;
                }
                if (!flag1)
                {
                    char[] arrayOfBuffer = buffer.toCharArray();
                    int indexForArrayOfBuffer = 0;
                    int i = indexForM, j;
                    while(true)
                    {
                        for (j = 0; j < 4; j++)
                        {
                            M[i][j] = arrayOfBuffer[indexForArrayOfBuffer];
                            indexForArrayOfBuffer++;
                            if (indexForArrayOfBuffer >= arrayOfBuffer.length)
                            {
                                flag2 = true;
                                break;
                            }
                        }
                        if (flag2)
                            break;
                        i++;
                    }

                    indexForM = i;

                    if (indexForM < 10)
                        indexForM = 10;
                    else if (indexForM < 20)
                        indexForM = 20;
                    else if (indexForM < 30)
                        indexForM = 30;
                    else if (indexForM < 40)
                        indexForM = 40;
                    else if (indexForM < 50)
                        indexForM = 50;
                    else if (indexForM < 60)
                        indexForM = 60;
                    else if (indexForM < 70)
                        indexForM = 70;
                    else if (indexForM < 80)
                        indexForM = 80;
                    else if (indexForM < 90)
                        indexForM = 90;
                    else
                    {
                        System.out.println("Error: Memory space exhausted. Exiting the program.");
                        System.exit(1);
                    }
                }
            }
            else
            {
                char[] arrayOfBuffer = buffer.toCharArray();
                int j = indexForM;
                for(int i = 0; i < arrayOfBuffer.length ; i++)
                {
                    M[j][i] = arrayOfBuffer[i];
                    if(i == 0)
                        indexForM++;
                }

                if (indexForM < 10)
                    indexForM = 10;
                else if (indexForM < 20)
                    indexForM = 20;
                else if (indexForM < 30)
                    indexForM = 30;
                else if (indexForM < 40)
                    indexForM = 40;
                else if (indexForM < 50)
                    indexForM = 50;
                else if (indexForM < 60)
                    indexForM = 60;
                else if (indexForM < 70)
                    indexForM = 70;
                else if (indexForM < 80)
                    indexForM = 80;
                else if (indexForM < 90)
                    indexForM = 90;
                else
                {
                    System.out.println("Error: Memory space exhausted. Exiting the program.");
                    System.exit(1);
                }
            }
            flag2 = false;
            flag1 = false;
        }
    }

    public static void main(String[] args)
    {
        File file = new File("C:\\Users\\Hp\\IdeaProjects\\Multiprogramming_OS\\InputForPhase1.txt");
        try
        {
            scanner = new Scanner(file);
        }
        catch (Exception e)
        {
            System.out.println("Error occurred while opening the file: " + e.getMessage());
            System.exit(1);
        }

        try
        {
            filewriter = new FileWriter("C:\\Users\\Hp\\IdeaProjects\\Multiprogramming_OS\\OutputForPhase1.txt");
        }
        catch (Exception e)
        {
            System.out.println("Error: Unable to open the file for writing. " + e.getMessage());
            System.exit(1);
        }

        Phase1.Init();
        Phase1.Load();

        try
        {
            filewriter.close();
        }
        catch (Exception e)
        {
            System.out.println("Error: Unable to close the file writer. " + e.getMessage());
        }
    }
}
