package knn;

/**
 *
 * @author ns
 */
public class NomeClasse {
    private String nomeClasse;
    private Double distancia;
    
    public NomeClasse() {
    }

    public NomeClasse(String nomeClasse, Double distancia) {
        this.nomeClasse = nomeClasse;
        this.distancia = distancia;
    }
    
    
    public String getNomeClasse() {
        return nomeClasse;
    }

    public void setNomeClasse(String nomeClasse) {
        this.nomeClasse = nomeClasse;
    }

    public Double getDistancia() {
        return distancia;
    }

    public void setDistancia(Double distancia) {
        this.distancia = distancia;
    }

    
    
}
