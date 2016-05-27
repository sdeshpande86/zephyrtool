package com.appdynamics.tool.jwt;

public class JwtHeader {
	protected String alg;
	protected String typ;

	public String getAlg() {
		return alg;
	}

	public void setAlg(String alg) {
		this.alg = alg;
	}

	public String getTyp() {
		return typ;
	}

	public void setTyp(String typ) {
		this.typ = typ;
	}
}