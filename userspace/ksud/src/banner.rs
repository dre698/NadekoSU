use figlet_rs::FIGlet;

pub fn print_banner() -> String {
    let logo_printer = FIGlet::standard().unwrap();

    logo_printer.convert("NadekoSU").unwrap().to_string()
}
