package br.fvc.beverage.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import br.fvc.beverage.R;
import br.fvc.beverage.adapter.AdapterProduto;
import br.fvc.beverage.helper.ConfiguracaoFirebase;
import br.fvc.beverage.helper.UsuarioFirebase;
import br.fvc.beverage.listener.RecyclerItemClickListener;
import br.fvc.beverage.model.Empresa;
import br.fvc.beverage.model.Produto;

public class EmpresaActivity extends AppCompatActivity {

    private FirebaseAuth autenticacao;
    private RecyclerView recyclerProdutos;
    private AdapterProduto adapterProduto;
    private List<Produto> produtos = new ArrayList<>();
    private DatabaseReference firebaseRef;
    private String idUsuarioLogado;
    Produto produtoSelecionado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_empresa);

        //Configurações iniciais
        inicializarComponentes();
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        firebaseRef = ConfiguracaoFirebase.getFirebase();
        idUsuarioLogado = UsuarioFirebase.getIdUsuario();

        //Configurações Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Beverage - Empresa");
        setSupportActionBar(toolbar);

        //Configura recyclerview
        recyclerProdutos.setLayoutManager(new LinearLayoutManager(this));
        recyclerProdutos.setHasFixedSize(true);
        adapterProduto = new AdapterProduto(produtos, this);
        recyclerProdutos.setAdapter(adapterProduto);

        //Recupera produtos para empresa
        recuperarProdutos();

        //Adiciona evento de clique no recyclerview
        recyclerProdutos.addOnItemTouchListener(
                new RecyclerItemClickListener(
                        this,
                        recyclerProdutos,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {

                            }

                            @Override
                            public void onLongItemClick(View view, int position) {
                                produtoSelecionado = produtos.get(position);//pega o produto pela posição
                                AlertDialog alerta;//cria um alerta pra pra confirmar a exclusão
                                AlertDialog.Builder builder = new AlertDialog.Builder(EmpresaActivity.this);
                                builder.setTitle("Excluir Bebida");
                                builder.setMessage("Tem certeza que deseja excluir a bebida?");
                                //define um botão como positivo
                                builder.setPositiveButton("Excluir", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface arg0, int arg1) {

                                        produtoSelecionado.remover();
                                        Toast.makeText(EmpresaActivity.this, "Excluído com sucesso", Toast.LENGTH_SHORT).show();
                                    }
                                });

                                builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface arg0, int arg1) {

                                    }
                                });
                                alerta = builder.create();
                                alerta.show();


                            }

                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                            }
                        }
                )
        );

    }

    private void recuperarProdutos() {

        DatabaseReference produtosRef = firebaseRef
                .child("produtos")
                .child(idUsuarioLogado);
        //recupera os produtos da empresa pelo idUsuarioLogado
        produtosRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                produtos.clear();

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

    private void inicializarComponentes() {
        recyclerProdutos = findViewById(R.id.recyclerProdutos);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_empresa, menu);

        MenuInflater inflater2 = getMenuInflater();
        inflater2.inflate(R.menu.example_menu, menu);//acho q tirei isso aq, n lembro

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menuSair:
                deslogarUsuario();
                break;
            case R.id.menuConfiguracoes:
                abrirConfiguracoes();
                break;
            case R.id.menuNovoProduto:
                abrirNovoProduto();
                break;
            case R.id.menuPedidos:
                abrirPedidos();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void deslogarUsuario() {
        try {
            autenticacao.signOut();//deslogar
            finish();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
//metodos abaixo para abrir a tela
    private void abrirPedidos() {
        startActivity(new Intent(EmpresaActivity.this, PedidosActivity.class));
    }

    private void abrirConfiguracoes() {
        startActivity(new Intent(EmpresaActivity.this, ConfiguracoesEmpresaActivity.class));
    }

    private void abrirNovoProduto() {
        startActivity(new Intent(EmpresaActivity.this, NovoProdutoEmpresaActivity.class));
    }

}
