package org.multiprogramming_os;

import java.io.File;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.Scanner;

public class Phase1
{
    // Memory array with 100 rows and 4 columns
    private static final char[][] M = new char[100][4];
    private static int IC;  // Instruction Counter
    private static final char[] IR = new char[4];  // Instruction Register
    private static boolean C;  // Condition Code
    private static final char[] R = new char[4];  // General-purpose Register
    private static int indexForM;  // Index for memory manipulation
    private static String buffer;  // Buffer for reading input
    private static FileWriter filewriter;  // FileWriter for output
    private static boolean flag1;  // Flag for instruction processing
    private static boolean flag2;  // Flag for memory manipulation
    private static Scanner scanner;  // Scanner for reading input file

    // Initialize memory, registers, and flags
    private static void Init()
    {
        // Initialize memory with '-' character
        for(char[] rowInMemory : M)
            Arrays.fill(rowInMemory, '-');

        // Initialize instruction register and general-purpose register
        Arrays.fill(IR, '-');
        Arrays.fill(R, '-');

        // Initialize counters and flags
        IC = 0;
        C = false;
        buffer = null;

        flag1 = false;
        flag2 = false;
        indexForM = 0;
    }

    // Read data from input and store in memory
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

    // Write data from memory to output file
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
            filewriter.flush();  // Ensure all data is written to file
        }
        catch(Exception e)
        {
            System.out.println("Error: Unable to write to the file. " + e.getMessage());
        }
    }

    // Terminate file writing with additional new lines
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

    // Machine Operating System (MOS) service routine
    private static void MOS(int SI)
    {
        switch(SI)
        {
            case 1:  // GD (Get Data)
                Phase1.Read();
                break;
            case 2:  // PD (Put Data)
                Phase1.Write();
                break;
            case 3:  // H (Halt)
                Phase1.Terminate();
                break;
        }
    }

    // Execute user program by processing instructions
    private static void ExecuteUserProgram()
    {
        while(true)
        {
            int SI;
            // Fetch instruction from memory
            for (int i = 0; i < 4; i++)
                IR[i] = M[IC][i];
            IC++;
            String opcode;

            // Determine opcode
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
            // Decode and execute the opcode
            switch (opcode)
            {
                case "LR":  // Load Register
                    for (int i = 0; i < 4; i++)
                        R[i] = M[operand][i];
                    break;
                case "SR":  // Store Register
                    for (int i = 0; i < 4; i++)
                        M[operand][i] = R[i];
                    break;
                case "CR":  // Compare Register
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
                case "BT":  // Branch on True
                    if (C)
                        IC = Integer.parseInt((("" + IR[2]) + IR[3]));
                    break;
                case "GD":  // Get Data
                    SI = 1;
                    Phase1.MOS(SI);
                    break;
                case "PD":  // Put Data
                    SI = 2;
                    Phase1.MOS(SI);
                    break;
                case "H":  // Halt
                    SI = 3;
                    Phase1.MOS(SI);
                    return;
            }
        }
    }

    // Start execution of the user program
    private static void StartExecution()
    {
        IC = 0;
        Phase1.ExecuteUserProgram();
    }

    // Load and process input instructions
    private static void Load()
    {
        while(scanner.hasNextLine())
        {
            buffer = scanner.nextLine();
            if (buffer.length() >= 4)
            {
                String first4char = buffer.substring(0, 4);
                if (first4char.equals("$AMJ"))  // Start of a new job
                {
                    flag1 = true;
                }
                else if (first4char.equals("$DTA"))  // Start of data section
                {
                    Phase1.StartExecution();
                    flag1 = true;
                }
                else if (first4char.equals("$END"))  // End of job
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

                    // Adjust memory index for the next block of data
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

                // Adjust memory index for the next block of data
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
    }

    public static void main(String[] args)
    {
        try
        {
            scanner = new Scanner(new File("program.txt"));
            filewriter = new FileWriter("output.txt");
        }
        catch(Exception e)
        {
            System.out.println("Error: Unable to open input/output file. " + e.getMessage());
            System.exit(1);
        }

        Phase1.Init();  // Initialize the system
        Phase1.Load();  // Load instructions and data
    }
}
