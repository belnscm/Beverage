package br.fvc.beverage.activity;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import br.fvc.beverage.R;
import br.fvc.beverage.adapter.AdapterPedido;
import br.fvc.beverage.helper.ConfiguracaoFirebase;
import br.fvc.beverage.helper.UsuarioFirebase;
import br.fvc.beverage.listener.RecyclerItemClickListener;
import br.fvc.beverage.model.Pedido;
import dmax.dialog.SpotsDialog;

public class PedidosActivity extends AppCompatActivity {

    private RecyclerView recyclerPedidos;
    private AdapterPedido adapterPedido;
    private List<Pedido> pedidos = new ArrayList<>();
    private AlertDialog dialog;
    private DatabaseReference firebaseRef;
    private String idEmpresa;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pedidos);

        //Configurações iniciais
        inicializarComponentes();
        firebaseRef = ConfiguracaoFirebase.getFirebase();
        idEmpresa = UsuarioFirebase.getIdUsuario();

        //Configuração Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Pedidos");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Configura recyclerview
        recyclerPedidos.setLayoutManager(new LinearLayoutManager(this));
        recyclerPedidos.setHasFixedSize(true);
        adapterPedido = new AdapterPedido(pedidos);
        recyclerPedidos.setAdapter(adapterPedido);

        recuperarPedidos();

        //Adiciona evento de clique no recyclerview
        recyclerPedidos.addOnItemTouchListener(
                new RecyclerItemClickListener(
                        this,
                        recyclerPedidos,
                        new RecyclerItemClickListener.OnItemClickListener(){
                            @Override
                            public void onItemClick(View view, int position) {

                            }

                            @Override
                            public void onLongItemClick(View view, int position) {
                                //longo click q finaliza o pedido
                                Pedido pedido = pedidos.get(position);
                                pedido.setStatus("Finalizado");
                                pedido.atualizarStatus();
                            }

                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                            }
                        }
                )
        );

    }

    private void recuperarPedidos() {
        //caixinha de carregamento
<<<<<<< HEAD
        /*dialog = new SpotsDialog.Builder()
=======
        dialog = new SpotsDialog.Builder()
>>>>>>> b335a5a4fbbaa38a4f93cd02e6c53f2f2af50843
                .setContext(this)
                .setMessage("Carregando dados")
                .setCancelable(false)
                .build();
<<<<<<< HEAD
        dialog.show();*/
=======
        dialog.show();
>>>>>>> b335a5a4fbbaa38a4f93cd02e6c53f2f2af50843

        DatabaseReference pedidoRef = firebaseRef
                .child("pedidos")
                .child(idEmpresa);

        Query pedidoPesquisa = pedidoRef.orderByChild("status")
                .equalTo("confirmado");

        pedidoPesquisa.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                pedidos.clear();
                if (dataSnapshot.getValue() != null) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        Pedido pedido = ds.getValue(Pedido.class);
                        pedidos.add(pedido);
                    }
                    adapterPedido.notifyDataSetChanged();
<<<<<<< HEAD
=======
                    dialog.dismiss();
>>>>>>> b335a5a4fbbaa38a4f93cd02e6c53f2f2af50843
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void inicializarComponentes() {
        recyclerPedidos = findViewById(R.id.recyclerPedidos);
    }

}
