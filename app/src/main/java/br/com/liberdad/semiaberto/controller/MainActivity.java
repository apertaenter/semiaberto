package br.com.liberdad.semiaberto.controller;

import android.app.AlarmManager;
import android.app.DialogFragment;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import br.com.liberdad.semiaberto.R;
import br.com.liberdad.semiaberto.model.Comparador;
import br.com.liberdad.semiaberto.model.Contrato;
import br.com.liberdad.semiaberto.model.Expediente;

public class MainActivity extends AppCompatActivity {

    private Menu menu;
    private ListView marcacoesListView;
    private ListViewAdapter listViewAdapter;
    private TextView saioAsTextView;
    private TextView atrasoTextView;
    private TextView debitoTextView;
    private TextView tempoTextView;
    private SeekBar bancoHorasSeekBar;

    private Expediente expediente;

    private PendingIntent pendingIntent;
/* DESATIVAR ALARME
    private boolean alarme = false;
    private boolean sonoro;
*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolBar = (Toolbar) findViewById(R.id.toolBar);
        toolBar.setTitle(R.string.app_name);
        toolBar.setTitleTextColor(0xFFFFFFFF);
        setSupportActionBar(toolBar);

        marcacoesListView = (ListView) findViewById(R.id.marcacoesListView);
        saioAsTextView = (TextView) findViewById(R.id.saioAsTextView);
        atrasoTextView = (TextView) findViewById(R.id.atrasoTextView);
        debitoTextView = (TextView) findViewById(R.id.debitoTextView);
        tempoTextView = (TextView) findViewById(R.id.tempoTextView);
        bancoHorasSeekBar = (SeekBar) findViewById(R.id.bancoHorasSeekBar);
        listViewAdapter = new ListViewAdapter(this);
        marcacoesListView.setAdapter(listViewAdapter);
        marcacoesListView.setDivider(null);
        marcacoesListView.setDividerHeight(0);
        View footerView = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.footer_view, null, false);
        marcacoesListView.addFooterView(footerView);
        bancoHorasSeekBar.setOnSeekBarChangeListener(new JornadaOnSeekBarChangeListener(this));

        SharedPreferences configuracoes = getPreferences(MODE_PRIVATE);
        int contrato = configuracoes.getInt("contrato", 8);
        boolean nucleoFlex = configuracoes.getBoolean("nucleoFlex", false);
        /* sonoro = configuracoes.getBoolean("sonoro", false);  DESATIVAR ALARME */

        expediente = new Expediente(nucleoFlex);

        setContrato(contrato);

        Set<String> marcacoes = configuracoes.getStringSet("marcacoes",null);
        Calendar calendario = Calendar.getInstance();
        calendario.set(Calendar.HOUR_OF_DAY,0);
        calendario.set(Calendar.MINUTE,0);
        calendario.set(Calendar.SECOND,0);
        long primeiraHora = calendario.getTimeInMillis();
        calendario.set(Calendar.HOUR_OF_DAY,23);
        calendario.set(Calendar.MINUTE,59);
        calendario.set(Calendar.SECOND,59);
        long ultimaHora = calendario.getTimeInMillis();
        if (marcacoes != null && !marcacoes.isEmpty()){
            for(String marcacao:marcacoes){
                long marcacaoLong = Long.parseLong(marcacao);
                if (marcacaoLong >= primeiraHora && marcacaoLong <= ultimaHora){
                    calendario.setTimeInMillis(marcacaoLong);
                    setMarcacao(calendario.get(Calendar.HOUR_OF_DAY),calendario.get(Calendar.MINUTE));
                }
            }
            if (!listViewAdapter.isEmpty()) {
                // Carregar jornada desejada
                int jornadaDesejada = configuracoes.getInt("jornadaDesejada", 0);
                setJornada(jornadaDesejada);
                bancoHorasSeekBar.setProgress(jornadaDesejada);
                // Carregar alarme
                /* DESATIVAR ALARME
                alarme = configuracoes.getBoolean("alarme", false);
                ImageView imageView = (ImageView) findViewById(R.id.alarmeImageView);

                if (!alarme) {
                    imageView.setImageResource(R.mipmap.alarm_off);
                    desativarAlarme();
                    alarme = false;
                } else {
                    imageView.setImageResource(R.mipmap.alarm_on);
                    ativarAlarme(expediente.getUltimaSaidaProposta());
                    alarme = true;
                }
                 */
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        this.menu = menu;

        MenuItem contrato6Item = (MenuItem) menu.getItem(0);
        MenuItem nucleoFlexItem = (MenuItem) menu.getItem(1);
        /* MenuItem sonoroItem = (MenuItem) menu.getItem(2); DESATIVAR ALARME */

        if (expediente.getContrato() == Contrato.SEIS) {
            contrato6Item.setChecked(true);
        } else {
            contrato6Item.setChecked(false);
        }

        if (expediente.isNucleoFlex()) {
            nucleoFlexItem.setChecked(true);
        } else {
            nucleoFlexItem.setChecked(false);
        }
        /* DESATIVAR ALARME
        if (sonoro) {
            sonoroItem.setChecked(true);
        }else{
            sonoroItem.setChecked(false);
        }
        */
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        MenuItem contrato6Item = (MenuItem) menu.getItem(0);
        MenuItem nucleoFlexItem = (MenuItem) menu.getItem(1);
        /* MenuItem alarmeSonoroItem = (MenuItem) menu.getItem(2); DESATIVAR ALARME */

        switch(item.getItemId()){
            case R.id.contrato6Item: // contrato6Item
                if (expediente.getContrato() == Contrato.SEIS) {
                    setContrato(8);
                    contrato6Item.setChecked(false);
                } else {
                    setContrato(6);
                    contrato6Item.setChecked(true);
                }
                break;
            case R.id.nucleoFlexItem: //nucleoFlexItem
                if (expediente.isNucleoFlex()){
                    setNucleoFlex(false);
                    nucleoFlexItem.setChecked(false);
                }else{

                    setNucleoFlex(true);
                    nucleoFlexItem.setChecked(true);
                }
                break;
            /* DESATIVAR ALARME
            case R.id.alarmeSonoroItem: //alarmeSonoroItem
                if (sonoro) {
                    sonoro = false;
                    alarmeSonoroItem.setChecked(false);
                }else {
                    sonoro = true;
                    alarmeSonoroItem.setChecked(true);
                }
                break;
             */
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        super.onStop();

        SharedPreferences configuracoes = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = configuracoes.edit();
        if (expediente.getContrato() == Contrato.OITO) {
            editor.putInt("contrato", 8);
        } else {
            editor.putInt("contrato", 6);
        }

        editor.putInt("jornadaDesejada", bancoHorasSeekBar.getProgress());

        editor.putBoolean("nucleoFlex", expediente.isNucleoFlex());

        /* DESATIVAR ALARME
        editor.putBoolean("sonoro", sonoro);

        editor.putBoolean("alarme", alarme);
        */

        Set<String> marcacoes = new HashSet<String>();

        for (Date marcacao:expediente.getMarcacoes()) {
            marcacoes.add(marcacao.getTime()+"");
        }
        editor.putStringSet("marcacoes",marcacoes);

        editor.commit();
    }

    public void adicionarMarcacao(View view) {
        DialogFragment dialogFragment = new TimePickerFragment();
        dialogFragment.show(getFragmentManager(), "TimePicker");

    }

    public void setMarcacao(int hora, int minuto) {

        Calendar calendario = Calendar.getInstance();
        calendario.set(Calendar.HOUR_OF_DAY, hora);
        calendario.set(Calendar.MINUTE, minuto);
        calendario.set(Calendar.SECOND, 0);
        calendario.set(Calendar.MILLISECOND, 0);

        Date marcacao;
        marcacao = calendario.getTime();

        listViewAdapter.add(marcacao);
        listViewAdapter.sort(new Comparador<Date>());

    }

    public void setMarcacoes(List<Date> marcacoes) {

        expediente.setMarcacoes(marcacoes);
        atualizarSaioAs(expediente.getUltimaSaidaProposta());
        verificarAtrasosEDebitos();

    }


    public void setJornada(int jornada) {

        setTempoTextView(bancoHorasSeekBar.getProgress());

        if (expediente.getContrato() == Contrato.OITO) {

            jornada += (5 * 60);

        } else {

            jornada += (4 * 60);

        }

        expediente.setJornada(jornada * 60 * 1000);
        atualizarSaioAs(expediente.getUltimaSaidaProposta());
        verificarAtrasosEDebitos();
    }

    public void setContrato(int contrato) {

        LinearLayout label6LinearLayout = (LinearLayout) findViewById(R.id.seisLinearLayout);
        LinearLayout label8LinearLayout = (LinearLayout) findViewById(R.id.oitoLinearLayout);

        if (contrato == 8) {
            label6LinearLayout.setVisibility(View.GONE);
            label8LinearLayout.setVisibility(View.VISIBLE);
            bancoHorasSeekBar.setMax(300);

            if (null == expediente.getContrato()) {
                bancoHorasSeekBar.setProgress(180);
                expediente.setJornada(8 * 60 * 60 * 1000); // Oito horas long
            } else {
                bancoHorasSeekBar.setProgress(bancoHorasSeekBar.getProgress() + 60);
                expediente.setJornada((bancoHorasSeekBar.getProgress() + 5 * 60) * 60 * 1000);
            }

        } else {
            label8LinearLayout.setVisibility(View.GONE);
            label6LinearLayout.setVisibility(View.VISIBLE);
            bancoHorasSeekBar.setMax(240);

            if (null == expediente.getContrato()) {
                bancoHorasSeekBar.setProgress(120);
                expediente.setJornada(6 * 60 * 60 * 1000); // Seis horas long
            } else {
                int posicaoAnterior = bancoHorasSeekBar.getProgress();
                if (posicaoAnterior < 60) {
                    bancoHorasSeekBar.setProgress(0);
                } else {
                    bancoHorasSeekBar.setProgress(posicaoAnterior - 60);
                }
                expediente.setJornada((bancoHorasSeekBar.getProgress() + 4 * 60) * 60 * 1000);
            }
        }

        expediente.setContrato(contrato);

        setTempoTextView(bancoHorasSeekBar.getProgress());

        atualizarSaioAs(expediente.getUltimaSaidaProposta());
        verificarAtrasosEDebitos();

    }

    public void setNucleoFlex(boolean nucleoFlex){

        expediente.setNucleoFlex(nucleoFlex);
        atualizarSaioAs(expediente.getUltimaSaidaProposta());
        verificarAtrasosEDebitos();

    }

    public void setTempoTextView(int minutos) {

        int posicaoRelativa = 0;
        int posicaoAbsoluta = 0;
        String saida = "";

        if (expediente.getContrato() == Contrato.OITO) {

            posicaoRelativa = minutos - 180;
            posicaoAbsoluta = 300 + minutos;

        } else {

            posicaoRelativa = minutos - 120;
            posicaoAbsoluta = 240 + minutos;

        }

        // Total da jornada desejada

        if ((posicaoAbsoluta / 60) < 10) {
            saida = saida + "0";
        }

        saida += (posicaoAbsoluta/60) + ":";

        if ((posicaoAbsoluta % 60) < 10) {
            saida = saida + "0";
        }
        saida += (posicaoAbsoluta%60);

        // Variação

        if (posicaoRelativa < 0) {
            saida += " (-";
        } else {
            saida += " (+";
        }


        saida = saida + "0" + Math.abs(posicaoRelativa / 60) + ":";
        if ((Math.abs(posicaoRelativa) % 60) < 10) {
            saida = saida + "0";
        }



        saida = saida + (Math.abs(posicaoRelativa) % 60);

        saida += ")";

        tempoTextView.setText(saida);

    }

    public void atualizarSaioAs(Date horario) {

        if (null == horario) {
            saioAsTextView.setText(R.string.saioas_text);
        } else {

            SimpleDateFormat formata = new SimpleDateFormat("HH:mm");
            saioAsTextView.setText(formata.format(horario));
        }
        /* DESATIVAR ALARME
        if (alarme){
            desativarAlarme();
            ativarAlarme(horario);
        }
         */
    }

    private void verificarAtrasosEDebitos() {

        if (expediente.isAtrasado()) {
            atrasoTextView.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.orange_200));
        } else {
            atrasoTextView.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.gray_white));
        }
        if (expediente.isDebito()) {
            debitoTextView.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.orange_200));
        } else {
            debitoTextView.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.gray_white));
        }

    }

    public void decrementarSeekBar(View view) {

        if (bancoHorasSeekBar.getProgress() > 0) {
            bancoHorasSeekBar.setProgress(bancoHorasSeekBar.getProgress() - 1);
            setJornada(bancoHorasSeekBar.getProgress());
        }

    }

    public void incrementarSeekBar(View view) {

        if (bancoHorasSeekBar.getProgress() < bancoHorasSeekBar.getMax()) {
            bancoHorasSeekBar.setProgress(bancoHorasSeekBar.getProgress() + 1);
            setJornada(bancoHorasSeekBar.getProgress());
        }
    }

    public void posicionarSeekBar(View view) {

        switch (view.getId()) {
            case R.id.cincoOitoImageView:
                bancoHorasSeekBar.setProgress(0);
                break;
            case R.id.seisOitoImageView:
                bancoHorasSeekBar.setProgress(60);
                break;
            case R.id.seteOitoImageView:
                bancoHorasSeekBar.setProgress(120);
                break;
            case R.id.oitoOitoImageView:
                bancoHorasSeekBar.setProgress(180);
                break;
            case R.id.noveOitoImageView:
                bancoHorasSeekBar.setProgress(240);
                break;
            case R.id.dezOitoImageView:
                bancoHorasSeekBar.setProgress(300);
                break;
            case R.id.quatroSeisImageView:
                bancoHorasSeekBar.setProgress(0);
                break;
            case R.id.cincoSeisImageView:
                bancoHorasSeekBar.setProgress(60);
                break;
            case R.id.seisSeisImageView:
                bancoHorasSeekBar.setProgress(120);
                break;
            case R.id.seteSeisImageView:
                bancoHorasSeekBar.setProgress(180);
                break;
            case R.id.oitoSeisImageView:
                bancoHorasSeekBar.setProgress(240);
                break;
        }
        setJornada(bancoHorasSeekBar.getProgress());

    }
/* DESATIVADO
    public void ativarDesativarAlarme(View view){

        ImageView imageView = (ImageView)findViewById(R.id.alarmeImageView);

        if (alarme){
            imageView.setImageResource(R.mipmap.alarm_off);
            desativarAlarme();
            alarme = false;
        }else{
            imageView.setImageResource(R.mipmap.alarm_on);
            ativarAlarme(expediente.getUltimaSaidaProposta());
            alarme = true;
        }

    }

    public void ativarAlarme(Date horario) {
        if(listViewAdapter.isEmpty() || horario.before(Calendar.getInstance().getTime())) {
            desativarAlarme();
        }else{

            Intent alarmIntent = new Intent(this, AlarmReceiver.class);
            alarmIntent.putExtra("sonoro",sonoro);
            pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            manager.setExact(AlarmManager.RTC_WAKEUP, horario.getTime(), pendingIntent);
        }
    }

    public void desativarAlarme() {
        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        manager.cancel(pendingIntent);
    }
*/
    public void abrirPlay(View view) {

        final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
        } catch (android.content.ActivityNotFoundException anfe) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
        }

    }

}
