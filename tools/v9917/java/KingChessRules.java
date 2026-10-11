package com.kingplus.social;

/** Pure, deterministic chess rules shared by live room clients. */
public final class KingChessRules {
  private KingChessRules(){}
  public static final String START="rnbqkbnrpppppppp................................PPPPPPPPRNBQKBNR";
  public static final class Result {
    public final String board,rights,status;
    public final int enPassant; public final char next;
    Result(String b,String r,int e,char t,String s){board=b;rights=r;enPassant=e;next=t;status=s;}
  }
  static boolean white(char p){return p>='A'&&p<='Z';}
  static boolean piece(char p){return p!='.';}
  static boolean same(char a,char b){return piece(a)&&piece(b)&&white(a)==white(b);}
  static int row(int i){return i/8;}static int col(int i){return i%8;}
  static boolean inside(int r,int c){return r>=0&&r<8&&c>=0&&c<8;}
  static boolean clear(char[] b,int from,int to){
    int dr=Integer.compare(row(to),row(from)),dc=Integer.compare(col(to),col(from));
    int r=row(from)+dr,c=col(from)+dc;
    while(r!=row(to)||c!=col(to)){
      if(!inside(r,c)||piece(b[r*8+c]))return false;r+=dr;c+=dc;
    }return true;
  }
  static boolean shape(char[] b,int from,int to,boolean attack){
    char p=b[from];if(from==to||p=='.')return false;
    int dr=row(to)-row(from),dc=col(to)-col(from),a=Math.abs(dr),d=Math.abs(dc);
    switch(Character.toLowerCase(p)){
      case 'p':return attack&&dr==(white(p)?-1:1)&&d==1;
      case 'n':return a==2&&d==1||a==1&&d==2;
      case 'k':return Math.max(a,d)==1;
      case 'r':return (dr==0||dc==0)&&clear(b,from,to);
      case 'b':return a==d&&clear(b,from,to);
      case 'q':return (a==d||dr==0||dc==0)&&clear(b,from,to);
      default:return false;
    }
  }
  static boolean attacked(char[] b,int at,boolean byWhite){
    for(int i=0;i<64;i++)if(piece(b[i])&&white(b[i])==byWhite&&shape(b,i,at,true))return true;
    return false;
  }
  static boolean check(char[] b,boolean w){
    int at=-1;for(int i=0;i<64;i++)if(b[i]==(w?'K':'k')){at=i;break;}
    return at<0||attacked(b,at,!w);
  }
  public static boolean inCheck(String b,char turn){return b!=null&&b.length()==64&&check(b.toCharArray(),turn=='w');}
  public static Result play(String board,char turn,String rights,int ep,int from,int to){
    return move(board,turn,rights,ep,from,to,true);
  }
  private static Result move(String board,char turn,String rights,int ep,int from,int to,boolean finish){
    if(board==null||board.length()!=64||from<0||from>=64||to<0||to>=64||turn!='w'&&turn!='b')
      throw new IllegalArgumentException("Invalid chess board or move");
    if(rights==null)rights="";
    char[] b=board.toCharArray();char p=b[from],target=b[to];boolean w=turn=='w';
    if(p=='.'||white(p)!=w||same(p,target)||Character.toLowerCase(target)=='k')
      throw new IllegalArgumentException("Not your piece or destination unavailable");
    int dr=row(to)-row(from),dc=col(to)-col(from),a=Math.abs(dr),d=Math.abs(dc);
    char kind=Character.toLowerCase(p);boolean passant=false;
    if(kind=='p'){
      int dir=w?-1:1;
      if(dc==0&&dr==dir&&target=='.'){}
      else if(dc==0&&dr==2*dir&&row(from)==(w?6:1)&&target=='.'&&b[from+8*dir]=='.'){}
      else if(d==1&&dr==dir&&target!='.'&&white(target)!=w){}
      else if(d==1&&dr==dir&&to==ep&&target=='.'&&b[to-8*dir]==(w?'p':'P'))passant=true;
      else throw new IllegalArgumentException("Illegal pawn move");
    }else if(kind=='k'&&dr==0&&d==2){
      boolean shortSide=dc>0;char permit=w?(shortSide?'K':'Q'):(shortSide?'k':'q');
      int home=w?60:4,rookFrom=home+(shortSide?3:-4),rookTo=home+(shortSide?1:-1);
      if(from!=home||rights.indexOf(permit)<0||b[rookFrom]!=(w?'R':'r')||target!='.'||
        b[home+(shortSide?1:-1)]!='.'||(!shortSide&&b[home-3]!='.')||
        check(b,w)||attacked(b,home+(shortSide?1:-1),!w)||attacked(b,to,!w))
        throw new IllegalArgumentException("Castling blocked or king in check");
      b[rookTo]=b[rookFrom];b[rookFrom]='.';
    }else if(!shape(b,from,to,false))throw new IllegalArgumentException("Illegal piece move");
    b[from]='.';b[to]=p;
    if(passant)b[to-(w?-8:8)]='.';
    if(kind=='p'&&row(to)==(w?0:7))b[to]=w?'Q':'q';
    if(check(b,w))throw new IllegalArgumentException("Your king would be in check");
    String r=rights;
    if(p=='K')r=r.replace("K","").replace("Q","");
    if(p=='k')r=r.replace("k","").replace("q","");
    if(from==63||to==63)r=r.replace("K","");
    if(from==56||to==56)r=r.replace("Q","");
    if(from==7||to==7)r=r.replace("k","");
    if(from==0||to==0)r=r.replace("q","");
    int newEp=kind=='p'&&a==2?(from+to)/2:-1;char next=w?'b':'w';
    String status="active";
    if(finish&&!hasMove(new String(b),next,r,newEp))status=check(b,!w)?"checkmate":"stalemate";
    return new Result(new String(b),r,newEp,next,status);
  }
  public static boolean hasMove(String b,char side,String rights,int ep){
    if(b==null||b.length()!=64)return false;
    for(int from=0;from<64;from++){
      char p=b.charAt(from);if(p=='.'||white(p)!=(side=='w'))continue;
      for(int to=0;to<64;to++)try{move(b,side,rights,ep,from,to,false);return true;}catch(IllegalArgumentException ignored){}
    }return false;
  }
  public static int square(String s){
    if(s==null||s.length()!=2)return -1;
    int col=Character.toLowerCase(s.charAt(0))-'a',rank=s.charAt(1)-'1';
    return inside(7-rank,col)?(7-rank)*8+col:-1;
  }
}