package br.fvc.beverage.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import br.fvc.beverage.R;
import br.fvc.beverage.adapter.AdapterProduto;
import br.fvc.beverage.helper.ConfiguracaoFirebase;
import br.fvc.beverage.helper.UsuarioFirebase;
import br.fvc.beverage.listener.RecyclerItemClickListener;
import br.fvc.beverage.model.Empresa;
import br.fvc.beverage.model.ItemPedido;
import br.fvc.beverage.model.Pedido;
import br.fvc.beverage.model.Produto;
import br.fvc.beverage.model.Usuario;
import dmax.dialog.SpotsDialog;

public class CardapioActivity extends AppCompatActivity {

    private RecyclerView recyclerProdutosCardapio;
    private ImageView imageEmpresaCardapio;
    private TextView textNomeEmpresaCardapio;
    private Empresa empresaSelecionada;
    private AlertDialog dialog;
    private TextView textCarrinhoQtd, textCarrinhoTotal;

    private AdapterProduto adapterProduto;
    private List<Produto> produtos = new ArrayList<>();
    private List<ItemPedido> itensCarrinho = new ArrayList<>();
    private DatabaseReference firebaseRef;
    private String idEmpresa;
    private String idUsuarioLogado;
    private Usuario usuario;
    private Pedido pedidoRecuperado;
    private int qtdItensCarrinho;
    private Double totalCarrinho;
    private int metodoPagamento;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cardapio);

        //Configurações iniciais
        inicializarComponentes();
        firebaseRef = ConfiguracaoFirebase.getFirebase();
        idUsuarioLogado = UsuarioFirebase.getIdUsuario();

        //Recuperar empresa selecionada
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            empresaSelecionada = (Empresa) bundle.getSerializable("empresa");

            textNomeEmpresaCardapio.setText(empresaSelecionada.getNome());
            idEmpresa = empresaSelecionada.getIdUsuario();//pegando o id da empresa

            String url = empresaSelecionada.getUrlImagem();
            Picasso.get().load(url).into(imageEmpresaCardapio);//pra carregar a imagem

        }

        //Configurações Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Cardápio");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Configuração recyclerview
        recyclerProdutosCardapio.setLayoutManager(new LinearLayoutManager(this));
        recyclerProdutosCardapio.setHasFixedSize(true);
        adapterProduto = new AdapterProduto(produtos, this);
        recyclerProdutosCardapio.setAdapter(adapterProduto);

        //Configurar evento de clique no recycler
        recyclerProdutosCardapio.addOnItemTouchListener(
                new RecyclerItemClickListener(
                        this,
                        recyclerProdutosCardapio,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {
                                confirmarQuantidade(position);
                            }

                            @Override
                            public void onLongItemClick(View view, int position) {

                            }

                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                            }
                        }
                )
        );

        //Recupera produtos para empresa
        recuperarProdutos();
        recuperarDadosUsuario();

    }

    private void  confirmarQuantidade(final int posicao) {
        //criando o alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Quantidade");
        builder.setMessage("Digite a quantidade");
        //criando um edit text para o alert dialog
        final EditText editQuantidade = new EditText(this);
        editQuantidade.setText("1"); //valor padrão

        builder.setView(editQuantidade);
        //clique do botão de confimar do alert dialog
        builder.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //pegando valor do edit
                String quantidade = editQuantidade.getText().toString();

                //ele pega o produto selecionado pelo clientee e coloca em itemPedido
                Produto produtoSelecionado = produtos.get(posicao);
                ItemPedido itemPedido = new ItemPedido();
                itemPedido.setIdProduto(produtoSelecionado.getIdProduto());
                itemPedido.setNomeProduto(produtoSelecionado.getNome());
                itemPedido.setPreco(produtoSelecionado.getPreco());
                itemPedido.setQuantidade(Integer.parseInt(quantidade));

                //adiciona o item pedido no carrinho
                itensCarrinho.add(itemPedido);

                if (pedidoRecuperado == null) {
                    pedidoRecuperado = new Pedido(idUsuarioLogado, idEmpresa);
                }
                //***pode dar erro****
                // pq falta colocar uma condição pra saber se o usuario configurou o nome e endereço
                pedidoRecuperado.setNome(usuario.getNome());
                pedidoRecuperado.setEndereco(usuario.getEndereco());
                pedidoRecuperado.setItens(itensCarrinho);
                pedidoRecuperado.salvar();


            }
        });

        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();

    }

    private void recuperarDadosUsuario() {
        //uma caixinha de carregamento
        dialog = new SpotsDialog.Builder()
                .setContext(this)
                .setMessage("Carregando dados")
                .setCancelable(false)
                .build();
        dialog.show();

        DatabaseReference usuariosRef = firebaseRef
                .child("usuarios")
                .child(idUsuarioLogado);
        //pega os dados do usuario pelo idUsuarioLogado
        usuariosRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    usuario = dataSnapshot.getValue(Usuario.class);
                }
                recuperPedido();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void recuperPedido() {

        DatabaseReference pedidoRef = firebaseRef
                .child("pedidos_usuario")
                .child(idEmpresa)
                .child(idUsuarioLogado);
        //recupera o pedido do cliente pelo idEmpresa e idUsuarioLogado
        pedidoRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                qtdItensCarrinho = 0;
                totalCarrinho = 0.0;
                itensCarrinho = new ArrayList<>();

                if (dataSnapshot.getValue() != null) {
                    //se for diferente de nulo, ele recupera os pedidos e taca no carrinho
                    pedidoRecuperado = dataSnapshot.getValue(Pedido.class);
                    itensCarrinho = pedidoRecuperado.getItens();


                    for (ItemPedido itemPedido : itensCarrinho) {
                        //pega os itens pedidos e faz o calculo baseado no preço e a quantidade e taca
                        // no carrinho
                        int qtde = itemPedido.getQuantidade();
                        Double preco = itemPedido.getPreco();

                        totalCarrinho += (qtde * preco);
                        qtdItensCarrinho += qtde;

                    }

                }

                DecimalFormat df = new DecimalFormat("0.00");//pra colocar o valor em decimal

                textCarrinhoQtd.setText("qtd: " + String.valueOf(qtdItensCarrinho));//setando a quantidade
                textCarrinhoTotal.setText("R$ " + df.format(totalCarrinho));//setando o valor

                dialog.dismiss();//fechando o dialog

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    private void recuperarProdutos() {

        DatabaseReference produtosRef = firebaseRef
                .child("produtos")
                .child(idEmpresa);
        //recupera os produtos da empresa pelo id da empresa
        produtosRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                produtos.clear();//primeiro limpa o array
                //estrutura de repetição pra encher o array com os produtos
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    produtos.add(ds.getValue(Produto.class));
                }

                adapterProduto.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_cardapio, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuPedido:
                confirmarPedido();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void confirmarPedido() {
        //criando alert
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Selecione um método de pagamento");

        CharSequence[] itens = new CharSequence[]{
                "Dinheiro", "Máquina cartão"
        };
        builder.setSingleChoiceItems(itens, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                metodoPagamento = which;
            }
        });
        //criando edit para o dialog
        final EditText editObservacao = new EditText(this);
        editObservacao.setHint("Digite uma observação");
        builder.setView(editObservacao);//mostrando o edit

        builder.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                String observacao = editObservacao.getText().toString();
                pedidoRecuperado.setMetodoPagamento(metodoPagamento);
                pedidoRecuperado.setObservacao(observacao);
                pedidoRecuperado.setStatus("confirmado");
                pedidoRecuperado.confimar();
                pedidoRecuperado.remover();
                pedidoRecuperado = null;

            }
        });
        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();

    }

    private void inicializarComponentes() {
        recyclerProdutosCardapio = findViewById(R.id.recyclerProdutosCardapio);
        imageEmpresaCardapio = findViewById(R.id.imageEmpresaCardapio);
        textNomeEmpresaCardapio = findViewById(R.id.textNomeEmpresaCardapio);
        textCarrinhoQtd = findViewById(R.id.textCarrinhoQtd);
        textCarrinhoTotal = findViewById(R.id.textCarrinhoTotal);

    }

}
