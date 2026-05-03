package com.release.klinikgaharumedika;

import com.release.klinikgaharumedika.view.LoginForm;
import javax.swing.SwingUtilities;

public class KlinikGaharuMedika {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginForm().setVisible(true));
    }
}
