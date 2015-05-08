package cafeint;

import java.awt.Color;
import java.util.ArrayList;

import javax.swing.BorderFactory;

import cafeint.Gastkarte.Geschlecht;
import cafeint.Gastkarte.Land;

public class Stuhl {
	
	private Gastkarte gast;
	private ArrayList<Tisch> tische = new ArrayList<Tisch>();
    private Spielzelle sz;
    private boolean partnerNoetig = false;
    
	public Gastkarte getGast() {
		return gast;
	}
	
	public boolean setGast(Gastkarte gasttemp) {
		Meldungen msgbox = new Meldungen();
		Spielzuege spz = new Spielzuege();
		if(!gastLandKorrekt(gasttemp)) {
			spz.warnungsboxtext(msgbox.gastlandfalsch);
			partnerNoetig = false;
			spz.stuehledemarkieren(false);
			return false;
		} else if(!gastGeschlechtKorrekt(gasttemp)) {
			if(gasttemp.getGeschlecht().equals(Geschlecht.Mann)) {
				spz.warnungsboxtext(msgbox.gastzuvielemaenner);
			} else {
				spz.warnungsboxtext(msgbox.gastzuvielefrauen);
			}
			partnerNoetig = false;
			spz.stuehledemarkieren(false);
			return false;
		} else if(!gastPartnerKorrekt(gasttemp)) {
			spz.warnungsboxtext(msgbox.gastpartnerfalsch);
			return false;
		} else {
			if(Variablenkammer.getZustand() == 12) {
				this.gast = gasttemp;
				this.sz.repaint();
				spz.tischedemarkieren();
				spz.warnungsboxreseten();
				if(partnerNoetig) {
					Variablenkammer.setZustand(10);
					spz.stuehledemarkieren(false);
					gruenfaerben();
				} else {
					Variablenkammer.setZustand(11);
					spz.stuehledemarkieren(true);
				}
				tischVollPruefen();
				return true;
			} else if(Variablenkammer.getZustand() == 10 || Variablenkammer.getZustand() == 11) {
				this.gast = gasttemp;
				this.sz.repaint();
				Variablenkammer.setZustand(21);
				spz.tischedemarkieren();
				spz.warnungsboxreseten();
				spz.stuehledemarkieren(true);
				tischVollPruefen();
				return true;
			} else {
				return false;
			}
		}
	}
	
	public void gastNachHause() {
		this.gast = null;
		this.sz.repaint();
	}
	
	public ArrayList<Tisch> getTische() {
		return tische;
	}
	
	public void addTisch(Tisch tisch) {
		this.tische.add(tisch);
	}
	
	public Spielzelle getSpielzelle() {
		return sz;
	}
	
	public void setSpielzelle(Spielzelle sz) {
		this.sz = sz;
	}

	public boolean isPartnerNoetig() {
		return partnerNoetig;
	}

	public void setPartnerNoetig(boolean partnerNoetig) {
		this.partnerNoetig = partnerNoetig;
	}
	
	public void gruenfaerben() {
		this.getSpielzelle().setBorder(BorderFactory.createLineBorder(new Color(0x3ADF00), 3));
	}
	
	private boolean gastLandKorrekt(Gastkarte gasttemp) {
		boolean korr = false;
		for(Tisch tisch:this.tische) {
			if(tisch.getLaenderkarte().getLand().equals(gasttemp.getLand()) || gasttemp.getLand().equals(Land.JOKER)) {
				korr = true;
			}
		}
		if(korr == false) {
			for(final Tisch tisch:this.tische) {
				tisch.getSpielzelle().setBorder(BorderFactory.createLineBorder(Color.red, 3));
				Thread thread = new Thread(new Runnable() {
					  @Override
					  public void run() {
						  try {
							  Thread.sleep(5000);
							  tisch.getSpielzelle().setBorder(BorderFactory.createLineBorder(Spielfeld.getHintgrdfarb(), 3));
							  } catch(InterruptedException e) {}
						  }
					  }
				);
				thread.start();
			}
		}
		return korr;
	}
	
	private boolean gastGeschlechtKorrekt(Gastkarte gasttemp) {
		boolean korr = true;
		for(Tisch tisch:this.tische) {
			int mann=0, frau=0;
			for(Stuhl stuhl:tisch.getStuehle()) {
				if(stuhl.getGast()!=null) {
					if(stuhl.getGast().getGeschlecht().equals(Geschlecht.Mann)) {
						mann++;
					} else {
						frau++;
					}
				}
			}
			if((gasttemp.getGeschlecht().equals(Geschlecht.Mann)) && (mann > frau) || (gasttemp.getGeschlecht().equals(Geschlecht.Frau)) && (frau > mann)) {
				korr = false;
				for(final Stuhl stuhl:tisch.getStuehle()) {
					if(stuhl.getGast()!=null) {
						stuhl.getSpielzelle().setBorder(BorderFactory.createLineBorder(Color.red, 3));
						Thread thread = new Thread(new Runnable() {
							  @Override
							  public void run() {
								  try {
									  Thread.sleep(5000);
									  if(!stuhl.isPartnerNoetig()) {
										  stuhl.getSpielzelle().setBorder(BorderFactory.createLineBorder(Spielfeld.getHintgrdfarb(), 3));
									  }
									  } catch(InterruptedException e) {}
								  }
							  }
						);
						thread.start();
					}
				}
			}
		}
		return korr;
	}
	
	private boolean gastPartnerKorrekt(Gastkarte gasttemp) {
		boolean korr = false;
		for(Tisch tisch:this.tische) {
			for(Stuhl stuhl:tisch.getStuehle()) {
				if(!stuhl.equals(this)) {
					if(stuhl.getGast()!=null) {
						partnerNoetig = false;
						korr = true;
						break;
					} else if (Variablenkammer.getZustand()==12) {
						for(Gastkarte handtemp:Variablenkammer.getSpieler(42).getHandkarten()) {
							if(handtemp!=null) {
								if(!handtemp.equals(gasttemp)) {
									if(tempLandKorrekt(handtemp,stuhl) == true && tempGeschlechtKorrekt(gasttemp,handtemp,stuhl)) {
										partnerNoetig = true;
										korr = true;
									}
								}
							}
						}
					}
				}
			}
		}
		return korr;
		//Beachte auch, dass der Spieler im Ersten Zug auch Einzelkarten legen darf bzw. dass immer zwei Zusammenpassen
	}
	
	private boolean tempLandKorrekt(Gastkarte handtemp,Stuhl stuhltemp) {
		boolean korr = false;
		for(Tisch tisch:stuhltemp.getTische()) {
			if(tisch.getLaenderkarte().getLand().equals(handtemp.getLand()) || handtemp.getLand().equals(Land.JOKER)) {
				korr = true;
			}
		}
		return korr;
	}
	
	private boolean tempGeschlechtKorrekt(Gastkarte gasttemp, Gastkarte handtemp,Stuhl stuhltemp) {
		boolean korr = true;
		for(Tisch tisch:stuhltemp.getTische()) {
			int mann=0, frau=0;
			for(Stuhl stuhl:tisch.getStuehle()) {
				if(stuhl.getGast()!=null) {
					if(stuhl.getGast().getGeschlecht().equals(Geschlecht.Mann)) {
						mann++;
					} else {
						frau++;
					}
				}
			}
			if(gasttemp.getGeschlecht().equals(Geschlecht.Mann)) {
				mann++;
			} else {
				frau++;
			}
			if(handtemp.getGeschlecht().equals(Geschlecht.Mann)) {
				mann++;
			} else {
				frau++;
			}
			if((gasttemp.getGeschlecht().equals(Geschlecht.Mann)) && (mann > frau) || (gasttemp.getGeschlecht().equals(Geschlecht.Frau)) && (frau > mann)) {
				korr = false;
			}
		}
		return korr;
	}
	
	private void tischVollPruefen() {
		for(Tisch tisch:this.getTische()) {
			int i=0;
			for(Stuhl stuhl:tisch.getStuehle()) {
				if(stuhl.getGast()!=null) {
					i++;
				}
			}
			if(i==4) {
				tisch.setZuleeren(true);
			}
		}
		for(Tisch tisch:this.getTische()) {
			if(tisch.isZuleeren()) {
				tisch.setLand(null);
				for(Stuhl stuhl:tisch.getStuehle()) {
					stuhl.gastNachHause();
				}
				if(Variablenkammer.getZustand() == 11) {
					Variablenkammer.setZustand(221);
				} else if(Variablenkammer.getZustand() == 10) {
					Variablenkammer.setZustand(220);
				}
			}
		}
	}
	
}