package knn;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Scanner;

/**
 *
 * @author ns
 */
public class KNN {

    private static int linhaTeste = 1200;
    private static int coluna = 24;
    private static int linhaTreino = 3600;
    private static int quantidadeClasses = 12;

    public static void main(String[] args) throws IOException {
        //carregar dados
        String[][] dadosTeste;
        String[][] dadosTreino;
        dadosTeste = leDadosArquivo("teste.data", linhaTeste);
        dadosTreino = leDadosArquivo("treino.data", linhaTreino);

        //gerar matriz de confusão
        String[][] matrizConfusao;
        matrizConfusao = geraMatrizConfusao();

        //normalizar os dados usando MIN-MAX
        for (int k = 0; k < linhaTeste; k++) {
            dadosTeste = normalizarDados(dadosTeste, k, coluna);
        }

        for (int k = 0; k < linhaTreino; k++) {
            dadosTreino = normalizarDados(dadosTreino, k, coluna);
        }

        //pegar um valor para o K
        Scanner scan = new Scanner(System.in);
        System.out.println("Digite um valor para K: ");
        int k = scan.nextInt();

        //aplicar o KNN
        double precisao;
        precisao = calculaKNN(k, dadosTeste, dadosTreino, matrizConfusao);
        System.out.println("\n");
        System.out.println("Precisao: " + precisao+"%");
    }

    /*
    Método responsável por ler dados de um arquivo
    e salvar em uma lista
     */
    public static String[][] leDadosArquivo(String nomeDoArquivo, int valorLinhas) throws FileNotFoundException, IOException {
        String[][] dados = new String[valorLinhas][coluna + 1];

        File arquivo = new File(nomeDoArquivo);
        int i = 0;
        try (InputStream in = new FileInputStream(arquivo)) {
            Scanner scan = new Scanner(in);
            while (scan.hasNext()) {
                String[] split = scan.nextLine().split(",");
                for (int j = 0; j < split.length; j++) {
                    dados[i][j] = split[j];
                }
                i++;
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return dados;
    }

    /*
    Método responsável por criar
    a matriz de confusão
     */
    public static String[][] geraMatrizConfusao() {
        String[][] matrizConfusao = new String[quantidadeClasses + 1][quantidadeClasses + 1];
        ArrayList<Integer> classes = new ArrayList<>();

        for (int i = 0; i < quantidadeClasses; i++) {
            classes.add(i);
        }
        matrizConfusao[0][0] = " ";
        for (int i = 1; i <= quantidadeClasses; i++) {
            for (int j = 1; j <= quantidadeClasses; j++) {
                matrizConfusao[i][0] = String.valueOf(classes.get(i - 1));
                matrizConfusao[0][j] = String.valueOf(classes.get(j - 1));
                if (matrizConfusao[i][j] == null) {
                    matrizConfusao[i][j] = "0";
                }
            }//for
        }//for

        return matrizConfusao;
    }

    /*
    Métdos responsável por normalizar os dados utilziando
    o MIN-MAX
     */
    public static String[][] normalizarDados(String[][] dados, int i, int limite) {
        double min = 1000, max = -2.0;

        //encontra min
        for (int j = 0; j < limite; j++) {
            if (Double.parseDouble(dados[i][j]) < min) {
                min = Double.parseDouble(dados[i][j]);
            }
        }

        //encontra max
        for (int j = 0; j < limite; j++) {
            if (Double.parseDouble(dados[i][j]) > max) {
                max = Double.parseDouble(dados[i][j]);
            }
        }

        //normaliza
        double sub;
        double valorNormalizado;
        for (int j = 0; j < limite; j++) {
            sub = Double.parseDouble(dados[i][j]) - min;
            valorNormalizado = sub / (max - min);
            dados[i][j] = String.valueOf(valorNormalizado);
        }

        return dados;
    }

    /*
    Método que calcula o menor valor do KNN
    dado um valor de K passado por parâmetro
     */
    public static double calculaKNN(int valorK, String[][] dadosTeste, String[][] dadosTreino, String[][] matrizConfusao) {
        ArrayList<String> classeEncontrada = new ArrayList<>();
        ArrayList<String> classeCorreta = new ArrayList<>();

        for (int i = 0; i < linhaTeste; i++) {
            System.out.println("KNN para instância teste: " + i + " de 1200.");
            String nomeDaClasseEncontrada = knnParaLinhaTeste(dadosTeste, dadosTreino, i, valorK);
            classeEncontrada.add(nomeDaClasseEncontrada);
            classeCorreta.add(dadosTeste[i][coluna]);

        }

        double precisao = dadosFinais(classeCorreta, classeEncontrada, dadosTeste, matrizConfusao);

        System.out.println("-----MATRIZ DE CONFUSAO----- ");
        imprimeMatrizConfusao(matrizConfusao);

        return precisao;
    }

    /*
    Método responsável para cada linha de Teste percorrer
    todas as linhas dos dados de treino. 
    Calcular a distância euclidiana e retornar a classe encontrada
     */
    public static String knnParaLinhaTeste(String[][] dadosTeste, String[][] dadosTreino, int i, int valorK) {
        ArrayList<NomeClasse> distanciaEuclidiana = new ArrayList<>();

        for (int iTreino = 0; iTreino < linhaTreino; iTreino++) {
            double distancia = calculaDistanciaEuclidiana(dadosTeste, dadosTreino, i, iTreino);

            String nomeClasse = dadosTreino[iTreino][coluna];
            NomeClasse nc = new NomeClasse(nomeClasse, distancia);
            distanciaEuclidiana.add(nc);
        }

        //ordena
        Collections.sort(distanciaEuclidiana, new Comparator<NomeClasse>() {
            @Override
            public int compare(NomeClasse o1, NomeClasse o2) {
                return o1.getDistancia().compareTo(o2.getDistancia());
            }
        });

        //encontra Nome da classe pertencente ao menor valor
        ArrayList<String> aplicaK = new ArrayList<>();

        for (int k = 0; k < valorK; k++) {
            aplicaK.add(distanciaEuclidiana.get(k).getNomeClasse());
        }
        Collections.sort(aplicaK);

        //encontra a frequencia mais comum de classe
        String nomeClasseFrequente = null;
        ArrayList<Integer> equivalenciaAplicaK = new ArrayList<>();

        if (valorK == 1) {
            nomeClasseFrequente = aplicaK.get(0);

        } else {

            int frequenciaJaneiro = Collections.frequency(aplicaK, " janeiro");
            int frequenciaFevereiro = Collections.frequency(aplicaK, " fevereiro");
            int frequenciaMarço = Collections.frequency(aplicaK, " marco");
            int frequenciaAbril = Collections.frequency(aplicaK, " abril");
            int frequenciaMaio = Collections.frequency(aplicaK, " maio");
            int frequenciaJunho = Collections.frequency(aplicaK, " junho");
            int frequenciaJulho = Collections.frequency(aplicaK, " julho");
            int frequenciaAgosto = Collections.frequency(aplicaK, " agosto");
            int frequenciaSetembro = Collections.frequency(aplicaK, " setembro");
            int frequenciaOutubro = Collections.frequency(aplicaK, " outubro");
            int frequenciaNovembro = Collections.frequency(aplicaK, " novembro");
            int frequenciaDezembro = Collections.frequency(aplicaK, " dezembro");

            ArrayList<Integer> maioresFrequencia = new ArrayList<>();
            maioresFrequencia.add(frequenciaJaneiro);
            maioresFrequencia.add(frequenciaFevereiro);
            maioresFrequencia.add(frequenciaMarço);
            maioresFrequencia.add(frequenciaAbril);
            maioresFrequencia.add(frequenciaMaio);
            maioresFrequencia.add(frequenciaJunho);
            maioresFrequencia.add(frequenciaJulho);
            maioresFrequencia.add(frequenciaAgosto);
            maioresFrequencia.add(frequenciaSetembro);
            maioresFrequencia.add(frequenciaOutubro);
            maioresFrequencia.add(frequenciaNovembro);
            maioresFrequencia.add(frequenciaDezembro);

            int maior = 0;
            for (int j = 0; j < maioresFrequencia.size(); j++) {
                if (maioresFrequencia.get(j) > maior) {
                    maior = maioresFrequencia.get(j);
                }
            }
            if (maior == frequenciaJaneiro) {
                nomeClasseFrequente = " janeiro";
            }
            if (maior == frequenciaFevereiro) {
                nomeClasseFrequente = " fevereiro";
            }
            if (maior == frequenciaMarço) {
                nomeClasseFrequente = " marco";
            }
            if (maior == frequenciaAbril) {
                nomeClasseFrequente = " abril";
            }
            if (maior == frequenciaMaio) {
                nomeClasseFrequente = " maio";
            }
            if (maior == frequenciaJunho) {
                nomeClasseFrequente = " junho";
            }
            if (maior == frequenciaJulho) {
                nomeClasseFrequente = " julho";
            }
            if (maior == frequenciaAgosto) {
                nomeClasseFrequente = " agosto";
            }
            if (maior == frequenciaSetembro) {
                nomeClasseFrequente = " setembro";
            }
            if (maior == frequenciaOutubro) {
                nomeClasseFrequente = " outubro";
            }
            if (maior == frequenciaNovembro) {
                nomeClasseFrequente = " novembro";
            }
            if (maior == frequenciaDezembro) {
                nomeClasseFrequente = " dezembro";
            }
        }

        return nomeClasseFrequente;
    }

    /*
    Método responsável por calcular a distancia euclidiana
    nos dadosTreino e dadosTeste
     */
    public static double calculaDistanciaEuclidiana(String[][] dadosTeste, String[][] dadosTreino, int i, int iTreino) {
        double quadrado = 0.0;

        for (int j = 0; j < coluna; j++) {
            double converteTeste = Double.parseDouble(dadosTeste[i][j]);
            double converteTreino = Double.parseDouble(dadosTreino[iTreino][j]);
            quadrado += Math.pow(converteTeste - converteTreino, 2);

        }
        double distancia = Math.sqrt(quadrado);

        return distancia;
    }

    /*
    Encontra a precisão em porcentagem dos acertos
    e atualiza a matria de confusão
     */
    public static double dadosFinais(ArrayList<String> classeCorreta, ArrayList<String> classeEncontrada, String[][] dadosTeste, String[][] matrizConfusao) {

        double numeroClassesCorreta = 0;
        double numeroClassesErradas = 0;

        for (int i = 0; i < classeCorreta.size(); i++) {
            String cCorreta = classeCorreta.get(i);
            String cEncontrada = classeEncontrada.get(i);

            int classe = 0;
            classe = nomeiaClasse(cCorreta);
            int encontrada = 0;
            encontrada = nomeiaClasse(cEncontrada);

            if (cCorreta.equals(cEncontrada)) {
                numeroClassesCorreta++;
                int valor = Integer.parseInt(matrizConfusao[classe + 1][classe + 1]);
                int novoValor = valor + 1;
                matrizConfusao[classe + 1][classe + 1] = String.valueOf(novoValor);

            } else {
                numeroClassesErradas++;
                int valor = Integer.parseInt(matrizConfusao[classe + 1][encontrada + 1]);
                int novoValor = valor + 1;
                matrizConfusao[classe + 1][encontrada + 1] = String.valueOf(novoValor);
            }

        }
        System.out.println("\n");
        System.out.println("Numero total de classes: " + classeCorreta.size());
        System.out.println("Quantidade total de acertos: " + numeroClassesCorreta);
        System.out.println("Quantidade total de erros: " + numeroClassesErradas);
        System.out.println("\n");

        double tamanhoDadosTeste = dadosTeste.length;
        double precisao = (numeroClassesCorreta / tamanhoDadosTeste) * 100;

        return precisao;
    }

    /*
    Método responsável por achar um valor equivalente ao nome da classe
    que é o mes do ano.
     */
    public static int nomeiaClasse(String nomeClasse) {
        int valorEquivalenteNomeClasse = 0;

        if (nomeClasse.equals(" janeiro")) {
            valorEquivalenteNomeClasse = 0;

        } else if (nomeClasse.equals(" fevereiro")) {
            valorEquivalenteNomeClasse = 1;

        } else if (nomeClasse.equals(" marco")) {
            valorEquivalenteNomeClasse = 2;

        } else if (nomeClasse.equals(" abril")) {
            valorEquivalenteNomeClasse = 3;

        } else if (nomeClasse.equals(" maio")) {
            valorEquivalenteNomeClasse = 4;

        } else if (nomeClasse.equals(" junho")) {
            valorEquivalenteNomeClasse = 5;

        } else if (nomeClasse.equals(" julho")) {
            valorEquivalenteNomeClasse = 6;

        } else if (nomeClasse.equals(" agosto")) {
            valorEquivalenteNomeClasse = 7;

        } else if (nomeClasse.equals(" setembro")) {
            valorEquivalenteNomeClasse = 8;

        } else if (nomeClasse.equals(" outubro")) {
            valorEquivalenteNomeClasse = 9;

        } else if (nomeClasse.equals(" novembro")) {
            valorEquivalenteNomeClasse = 10;

        } else if (nomeClasse.equals(" dezembro")) {
            valorEquivalenteNomeClasse = 11;

        }
        return valorEquivalenteNomeClasse;
    }


    /*
    Método responsável por mostrar na tela
    a Matriz de confusão final
     */
    private static void imprimeMatrizConfusao(String[][] matriz) {
        for (int i = 0; i <= quantidadeClasses; i++) {
            for (int j = 0; j <= quantidadeClasses; j++) {
                System.out.print(matriz[i][j] + " ");
            }
            System.out.println(" ");
        }
    }
}
