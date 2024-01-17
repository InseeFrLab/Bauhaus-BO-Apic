package fr.insee;

public class SampleApic {

    public static void main(String[] args) {
        var newArgs=new String[args.length+1];
        System.arraycopy(args, 0, newArgs, 1, args.length);
        newArgs[0]="--fr.insee.apic.interface-controllers.package=fr.insee.rmes.bauhaus.controllers";
        ApiConsultation.main(newArgs);
    }
}
