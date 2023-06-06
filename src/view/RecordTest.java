/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package view;


import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;
import javax.swing.JOptionPane;


public class RecordTest extends javax.swing.JFrame {
    public RecordTest(){
        try{
        AudioFormat audioFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,44100,16,2,4,44100,false);
            DataLine.Info dataInfo = new DataLine.Info(TargetDataLine.class, audioFormat);
            if(!AudioSystem.isLineSupported(dataInfo)){
                System.out.println("non supporté");
            }
            TargetDataLine targetLine = (TargetDataLine)AudioSystem.getLine(dataInfo);
            targetLine.open();
            JOptionPane.showMessageDialog(null, "Cliquer sur OK pour commencer l'enregistrement");
            targetLine.start();
           
            Thread audioRecordedThread = new Thread()
            {
                @Override public void run(){
                    AudioInputStream recordingStream = new AudioInputStream(targetLine);
                    File outputFile = new File("audios/record.wav");
                    try{
                        AudioSystem.write(recordingStream, AudioFileFormat.Type.WAVE, outputFile);
                        
                    } catch (IOException ex) {
                        System.out.println(ex);
                    }
                    System.out.println("Enregistrement arrêté");
                }
            };
            audioRecordedThread.start();
            JOptionPane.showMessageDialog(null, "Cliquer sur OK pour arrêter l'enregistrement");
            targetLine.stop();
            targetLine.close();    
        }
        catch(Exception e){
            System.out.println(e);
        }
    }
    public static void main(String[] args) {
        
        new RecordTest().setVisible(true);
    }
}
