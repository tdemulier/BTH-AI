// BEGIN LICENSE BLOCK
// Version: CMPL 1.1
//
// The contents of this file are subject to the Cisco-style Mozilla Public
// License Version 1.1 (the "License"); you may not use this file except
// in compliance with the License.  You may obtain a copy of the License
// at www.eclipse-clp.org/license.
// 
// Software distributed under the License is distributed on an "AS IS"
// basis, WITHOUT WARRANTY OF ANY KIND, either express or implied.  See
// the License for the specific language governing rights and limitations
// under the License. 
// 
// The Original Code is  The ECLiPSe Constraint Logic Programming System. 
// The Initial Developer of the Original Code is  Cisco Systems, Inc. 
// Portions created by the Initial Developer are
// Copyright (C) 2001 - 2006 Cisco Systems, Inc.  All Rights Reserved.
// 
// Contributor(s): Josh Singer, Parc Technologies
// 
// END LICENSE BLOCK

//Title:        Java/ECLiPSe interface
//Version:      $Id: QuickTest.java,v 1.1.1.1 2006/09/23 01:54:13 snovello Exp $
//Author:       Josh Singer
//Company:      Parc Technologies
//Description:  Java/ECLiPSe Interface example Java program
import com.parctechnologies.eclipse.*;
import java.io.*;

public class QuickTest
{
  public static void main(String[] args) throws Exception
  {
    // Create some default Eclipse options
    EclipseEngineOptions eclipseEngineOptions = new EclipseEngineOptions(new File("D:\\Users\\Orhin\\Documents\\NetBeansProjects\\BTH-AI\\ECLiPSe 6.1"));
    
    // Object representing the Eclipse process
    EclipseEngine eclipse;

    // Connect the Eclipse's standard streams to the JVM's
    eclipseEngineOptions.setUseQueues(false);

    // Initialise Eclipse
    eclipse = EmbeddedEclipse.getInstance(eclipseEngineOptions);

    // Write a message
    //eclipse.rpc("write(output, 'hello world'), flush(output)");
    
    File eclipseProgram = new File("src\\wumpusworld\\test.pl");
    eclipse.compile(eclipseProgram);
        
    CompoundTerm result = eclipse.rpc("test2(X, Y)");

    // The top-level functor of the goal term is ",". 
    // The first and second arguments of the goal term are the two subgoals
    // and we can safely cast these as CompoundTerms.
    //CompoundTerm firstGoal = (CompoundTerm) result.arg(1);
    // X is the first argument of the first goal.
    Object firstGoalFirstArg = result.arg(1);
    Object secondGoalFirstArg = result.arg(2);
    // Y is the first argument of the second goal.

    System.out.println("X = "+firstGoalFirstArg);
    System.out.println("Y = "+secondGoalFirstArg);

    // Destroy the Eclipse process
    ((EmbeddedEclipse) eclipse).destroy();
  }
}
