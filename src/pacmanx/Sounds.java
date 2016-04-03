/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pacmanx;

import java.applet.Applet;
import java.applet.AudioClip;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author harth
 */
public class Sounds {

    AudioClip intro;

    public Sounds() {
        URL url = Sounds.class.getResource("pacmanx/intro sound.wav");
        this.intro = Applet.newAudioClip(url);
        System.out.println("end");
    }

    public void playIntro() {
        intro.play();
    }

    public void stopIntro() {
        intro.stop();
    }
}
