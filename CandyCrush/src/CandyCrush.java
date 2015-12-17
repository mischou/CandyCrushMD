import java.awt.*;
import java.awt.geom.*;
import java.util.*;
import java.awt.event.*;
import java.io.*;

class CandyCrush extends Panel implements Runnable, MouseListener, MouseMotionListener {
	
	// grille avec un numero de couleur par case
	int grid[][] = new int[8][8];
	// pour marquer les cases non alignees
	boolean marked[][] = new boolean[8][8];
	// couleur des cases : 0 = vide
	Color colors[] = { Color.WHITE, Color.RED, Color.GREEN, Color.BLUE, Color.GRAY, Color.PINK, Color.CYAN };
	// coordonnees des cases selectionnees : -1 = non selectionne
	int selectedX = -1, selectedY = -1;
	int swappedX = -1, swappedY = -1;

	// image pour le rendu hors ecran
	Image buffer;

	// initialisation : evenements souris et boucle principale
	CandyCrush() {
		// remplir une premiere fois la grille
		while (fill())
			;
		// enlever les alignements existants
		while (removeAlignments()) {
			fill();
		}
		addMouseListener(this);
		addMouseMotionListener(this);
		new Thread(this).start();
	}

	// gestion des evenements souris
	public void mousePressed(MouseEvent e) {
		// on appuie sur le bouton de la souris : recuperer les coordonnees de
		// la premiere case
		selectedX = e.getX() / 32;
		selectedY = e.getY() / 32;
		repaint();
	}

	public void mouseMoved(MouseEvent e) {
		// on bouge la souris : recuperer les coordonnees de la deuxieme case
		if (selectedX != -1 && selectedY != -1) {
			swappedX = e.getX() / 32;
			swappedY = e.getY() / 32;
			// si l'echange n'est pas valide, on cache la deuxieme case
			if (!isValidSwap(selectedX, selectedY, swappedX, swappedY)) {
				swappedX = swappedY = -1;
			}
		}
		repaint();
	}

	public void mouseReleased(MouseEvent e) {
		// lorsque l'on releche la souris il faut faire l'echange et cacher les
		// cases
		if (selectedX != -1 && selectedY != -1 && swappedX != -1 && swappedY != -1) {
			swap(selectedX, selectedY, swappedX, swappedY);
		}
		selectedX = selectedY = swappedX = swappedY = -1;
		repaint();
	}

	// non implementes
	public void mouseClicked(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mouseDragged(MouseEvent e) {
		mouseMoved(e);
	}

	// est-ce qu'on a trois cases de la meme couleur vers le droite depuis (i,
	// j) ?
	boolean horizontalAligned(int i, int j) {
		if (i < 0 || j < 0 || i >= 6 || j >= 8)
			return false;
		if (grid[i][j] == grid[i + 1][j] && grid[i][j] == grid[i + 2][j])
			return true;
		return false;
	}

	// est-ce qu'on a trois cases de la meme couleur vers le bas depuis (i, j) ?
	boolean verticalAligned(int i, int j) {
		if (i < 0 || j < 0 || i >= 8 || j >= 6)
			return false;
		if (grid[i][j] == grid[i][j + 1] && grid[i][j] == grid[i][j + 2])
			return true;
		return false;
	}

	// echanger le contenu de deux cases
	void swap(int x1, int y1, int x2, int y2) {
		int tmp = grid[x1][y1];
		grid[x1][y1] = grid[x2][y2];
		grid[x2][y2] = tmp;
	}

	// determine si l'echange entre deux cases est valide
	boolean isValidSwap(int x1, int y1, int x2, int y2) {
		// il faut que les cases soient dans la grille
		if (x1 == -1 || x2 == -1 || y1 == -1 || y2 == -1)
			return false;
		// que les cases soient e cete l'une de l'autre
		if (Math.abs(x2 - x1) + Math.abs(y2 - y1) != 1)
			return false;
		// et que les couleurs soient differentes
		if (grid[x1][y1] == grid[x2][y2])
			return false;

		// alors on effectue l'echange
		swap(x1, y1, x2, y2);

		// et on verifie que ea cree un nouvel alignement
		boolean newAlignment = false;
		for (int i = 0; i < 3; i++) {
			newAlignment |= horizontalAligned(x1 - i, y1);
			newAlignment |= horizontalAligned(x2 - i, y2);
			newAlignment |= verticalAligned(x1, y1 - i);
			newAlignment |= verticalAligned(x2, y2 - i);
		}

		// puis on annule l'echange
		swap(x1, y1, x2, y2);
		return newAlignment;
	}

	// supprimer les alignements
	boolean removeAlignments() {
		// passe 1 : marquer tous les alignements
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				if (grid[i][j] != 0 && horizontalAligned(i, j)) {
					marked[i][j] = marked[i + 1][j] = marked[i + 2][j] = true;
				}
				if (grid[i][j] != 0 && verticalAligned(i, j)) {
					marked[i][j] = marked[i][j + 1] = marked[i][j + 2] = true;
				}
			}
		}
		// passe 2 : supprimer les cases marquees
		boolean modified = false;
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				if (marked[i][j]) {
					grid[i][j] = 0;
					marked[i][j] = false;
					modified = true;
				}
			}
		}
		return modified;
	}

	// remplir les cases vides par gravite, et generer des cases aleatoirement
	// par le haut
	boolean fill() {
		Random rand = new Random();
		boolean modified = false;
		for (int i = 0; i < 8; i++) {
			for (int j = 7; j >= 0; j--) {
				if (grid[i][j] == 0) {
					if (j == 0)
						grid[i][j] = 1 + rand.nextInt(colors.length - 1);
					else {
						grid[i][j] = grid[i][j - 1];
						grid[i][j - 1] = 0;
					}
					modified = true;
				}
			}
		}
		return modified;
	}

	// evite le scintillement
	public void update(Graphics g) {
		paint(g);
	}

	// routine d'affichage : on fait du double buffering
	public void paint(Graphics g2) {
		if (buffer == null)
			buffer = createImage(800, 600);
		Graphics2D g = (Graphics2D) buffer.getGraphics();

		// fond
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, getWidth(), getHeight());

		// afficher la grille vide
		g.setColor(Color.BLACK);
		for (int i = 0; i < 9; i++) {
			g.drawLine(32 * i, 0, 32 * i, 8 * 32 + 1);
			g.drawLine(0, 32 * i, 8 * 32 + 1, 32 * i);
		}

		// afficher la premiere case selectionnee
		if (selectedX != -1 && selectedY != -1) {
			g.setColor(Color.ORANGE);
			g.fillRect(selectedX * 32 + 1, selectedY * 32 + 1, 31, 31);
		}

		// afficher la deuxieme case selectionnee
		if (swappedX != -1 && swappedY != -1) {
			g.setColor(Color.YELLOW);
			g.fillRect(swappedX * 32 + 1, swappedY * 32 + 1, 31, 31);
		}

		// afficher le contenu de la grille
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				g.setColor(colors[grid[i][j]]);
				g.fillOval(32 * i + 3, 32 * j + 3, 27, 27);
			}
		}

		// copier l'image e l'ecran
		g2.drawImage(buffer, 0, 0, null);
	}

	// boucle principale
	public void run() {
		while (true) {
			// un pas de simulation toutes les 100ms
			try {
				Thread.currentThread().sleep(100);
			} catch (InterruptedException e) {
			}

			// s'il n'y a pas de case vide, chercher des alignements
			if (!fill()) {
				removeAlignments();
			}

			// redessiner
			repaint();
		}
	}

	// taille de la fenetre
	public Dimension getPreferredSize() {
		return new Dimension(32 * 8 + 1, 32 * 8 + 1);
	}

	// met le jeu dans une fenetre
	public static void main(String args[]) {
		Frame frame = new Frame("Miam, des bonbons !");
		final CandyCrush obj = new CandyCrush();
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent event) {
				System.exit(0);
			}
		});
		frame.add(obj);
		frame.pack();
		frame.setVisible(true);
	}
}