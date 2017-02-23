#include <stdio.h>
#include <string.h>
#include <math.h>
#include <stdlib.h>

int main() {

    char text[10000];
    char stack[10];
    int top=0;
    int curPtr=0;
    int sent_end = 0;
    int num_sent = 0;
    int tmp,tlen;
    gets(text);
    stack[top] = 'S';
    while(curPtr < strlen(text)){
        
        if(text[curPtr] == '.'){
            if(stack[top] == 'S'){
            tmp = curPtr;
            tlen = 0;
            while(text[tmp] != ' '){
                tlen++;tmp--;
            }
            if(tlen <= 3 && isupper(text[++tmp]) > 0)
        sent_end = 0;
            else
                sent_end = 1;
            }
        }
        else if(text[curPtr] == '!' || text[curPtr] == '?')
            {
            if(stack[top] == 'S')
            sent_end = 1;
        }
        
        else if(text[curPtr] == '"' || text[curPtr] == '\''){
            if(text[curPtr] == '\'' && text[curPtr - 1] != ' '){
               sent_end = 0; 
            }else{
            if(stack[top] == 'S')
            {stack[++top] = '1';
           
            }
                else{
            top--;
                     
                }
            }
        }
        
        else if(text[curPtr] == '(' || text[curPtr] == ')'){
            if(stack[top] == 'S')
                stack[++top] = '2';
            else
                top--;
        }
        if(sent_end == 1){
            printf("%c",text[curPtr++]);
            num_sent++;
            printf("\n");
            sent_end = 0;
            if(text[curPtr] == ' ')
            curPtr++;
        }
        else{
            printf("%c",text[curPtr++]);
        }
    }
    //printf("%s",text);
    return 0;
}

