import {Dialog, DialogContent, DialogFooter, DialogHeader, DialogTitle,} from "@/components/ui/dialog";
import {Button} from "@/components/ui/button";

export default function DialogWrapper({children, isOpen, setIsOpen, onSubmit, title}: any) {
    const handleSave = async () => {
        onSubmit();
        setIsOpen(false);
    };

    return (
        <Dialog open={isOpen} onOpenChange={setIsOpen}>
            <DialogContent className="h-[70vh] w-[70vw] max-w-[800px]">
                <DialogHeader>
                    <DialogTitle>{title}</DialogTitle>
                </DialogHeader>
                {children}
                <DialogFooter>
                    <Button variant="secondary" onClick={() => setIsOpen(false)}>Close</Button>
                    {onSubmit && <Button onClick={handleSave}>Save</Button>}
                </DialogFooter>
            </DialogContent>
        </Dialog>
    );
}
