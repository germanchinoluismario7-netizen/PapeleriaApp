package com.example.papeleriaapp.presentation.selectRole

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.papeleriaapp.R

@Composable
fun SelectRoleScreen(
    onGoToRegisterSeller : () -> Unit,
    onGoToRegisterCustomer : () -> Unit
){
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ){
        Text(
            text = "Papelería App",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Elige cómo quieres registrarte",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        //Registro del Vendedor
        Card(
            modifier = Modifier.padding(all = 16.dp)
                .clickable{
                    onGoToRegisterSeller()
                }
        ){
            Column(
                modifier = Modifier.padding(all=16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter= painterResource(id=R.drawable.ic_seller),
                    contentDescription = "Registrarme como vendedor",
                    modifier=Modifier.size(120.dp)
                )
                Spacer(modifier=Modifier.height(8.dp))
                Text(text="Registrarme como Vendedor")
            }
        }

        //Regoistro del Cliente
        Card(
            modifier = Modifier.padding(all = 16.dp)
                .clickable{
                    onGoToRegisterCustomer()
                }
        ){
            Column(
                modifier = Modifier.padding(all=16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter= painterResource(id=R.drawable.ic_customer),
                    contentDescription = "Registrarme como Cliente",
                    modifier=Modifier.size(129.dp)
                )
                Spacer(modifier=Modifier.height(8.dp))
                Text(text="Registrarme como Cliente")
            }
        }
    }




}